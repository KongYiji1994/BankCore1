package com.bankcore.payment.service;

import com.bankcore.common.dto.AccountDTO;
import com.bankcore.common.dto.PaymentBatchResult;
import com.bankcore.common.dto.PaymentRequest;
import com.bankcore.common.dto.PaymentStatus;
import com.bankcore.common.error.BusinessException;
import com.bankcore.common.error.ErrorCode;
import com.bankcore.payment.client.AccountClient;
import com.bankcore.payment.client.CustomerClient;
import com.bankcore.payment.model.PaymentInstruction;
import com.bankcore.payment.model.PaymentRequestRecord;
import com.bankcore.payment.model.PaymentRequestStatus;
import com.bankcore.payment.repository.PaymentRepository;
import com.bankcore.payment.repository.PaymentRequestRepository;
import com.bankcore.payment.service.messaging.PaymentEventPublisher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付指令服务：负责受理请求、做幂等检查、落库并投递异步事件，支持批量、补偿与状态更新。
 */
@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final long REQUEST_LOCK_TTL_SECONDS = 120L;
    private final PaymentRepository repository;
    private final PaymentRequestRepository requestRepository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentIdempotencyManager idempotencyManager;
    private final AsyncTaskExecutor paymentTaskExecutor;

    /**
     * 构造注入依赖：仓储、幂等管理、外部账户/客户查询与事件发布器。
     */
    public PaymentService(PaymentRepository repository,
                          PaymentRequestRepository requestRepository,
                          AccountClient accountClient,
                          CustomerClient customerClient,
                          PaymentEventPublisher paymentEventPublisher,
                          PaymentIdempotencyManager idempotencyManager,
                          @Qualifier("paymentTaskExecutor") AsyncTaskExecutor paymentTaskExecutor) {
        this.repository = repository;
        this.requestRepository = requestRepository;
        this.accountClient = accountClient;
        this.customerClient = customerClient;
        this.paymentEventPublisher = paymentEventPublisher;
        this.idempotencyManager = idempotencyManager;
        this.paymentTaskExecutor = paymentTaskExecutor;
    }

    /**
     * 提交支付请求：通过 Redis 锁和请求表保障幂等，校验账户与客户状态后落库并投递 MQ。
     */
    @Transactional
    public PaymentInstruction submit(PaymentRequest request) {
        boolean lockAcquired = idempotencyManager.tryAcquireRequestLock(request.getRequestId(), REQUEST_LOCK_TTL_SECONDS);
        if (!lockAcquired) {
            log.info("request {} already locked, returning existing result if present", request.getRequestId());
            return resolveExistingRequest(request.getRequestId());
        }
        try {
            PaymentRequestRecord existing = requestRepository.findByRequestId(request.getRequestId()).orElse(null);
            if (existing != null) {
                return handleExistingRequest(existing);
            }
            AccountDTO payerAccount = accountClient.getAccount(request.getPayerAccount());
            if (payerAccount == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "Payer account not found");
            }
            CustomerClient.CustomerProfile payerCustomer = customerClient.getCustomer(payerAccount.getCustomerId());
            if (payerCustomer == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "Customer not found for account");
            }
            String payerStatus = payerCustomer.getStatus() == null ? "NORMAL" : payerCustomer.getStatus();
            if ("BLOCKED".equalsIgnoreCase(payerStatus)) {
                throw new BusinessException(ErrorCode.RISK_REJECTED, "Payer customer is blocked");
            }
            PaymentInstruction instruction = new PaymentInstruction(
                    request.getRequestId(),
                    request.getInstructionId(),
                    request.getPayerAccount(),
                    request.getPayeeAccount(),
                    payerAccount.getCustomerId(),
                    payerStatus,
                    request.getCurrency(),
                    request.getAmount(),
                    request.getPurpose(),
                    request.getChannel(),
                    request.getBatchId(),
                    request.getPriority(),
                    PaymentStatus.PENDING);
            log.info("enqueuing payment requestId={}, instructionId={}, payerAccount={}, amount={}",
                    instruction.getRequestId(), instruction.getInstructionId(), instruction.getPayerAccount(), instruction.getAmount());
            repository.save(instruction);
            requestRepository.createPending(request.getRequestId(), instruction.getInstructionId());
            paymentEventPublisher.publishAsync(request.getRequestId(), instruction.getInstructionId());
            return instruction;
        } finally {
            idempotencyManager.releaseRequestLock(request.getRequestId());
        }
    }

    /**
     * 手工补投 MQ：用于运营/批量重新触发处理。
     */
    public PaymentInstruction enqueueForProcessing(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
        log.info("manual enqueue instructionId={}, requestId={}", instructionId, instruction.getRequestId());
        requestRepository.updateStatus(instruction.getRequestId(), PaymentRequestStatus.PENDING, instructionId, "manual enqueue");
        paymentEventPublisher.publishAsync(instruction.getRequestId(), instructionId);
        return instruction;
    }

    /**
     * 批量触发支付处理，去重后逐个入队。
     */
    public PaymentBatchResult processBatch(List<String> instructionIds) {
        if (instructionIds == null || instructionIds.isEmpty()) {
            return new PaymentBatchResult(0, 0, 0, 0, Collections.emptyList());
        }
        List<String> distinct = instructionIds.stream().distinct().collect(Collectors.toList());
        AtomicInteger succeeded = new AtomicInteger();
        List<String> failedIds = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        for (String id : distinct) {
            futures.add(paymentTaskExecutor.submit(() -> {
                try {
                    enqueueForProcessing(id);
                    succeeded.incrementAndGet();
                } catch (BusinessException ex) {
                    failedIds.add(id);
                    log.warn("batch enqueue rejected for instructionId={}, reason={}", id, ex.getMessage());
                } catch (Exception ex) {
                    failedIds.add(id);
                    log.error("batch enqueue failed for instructionId=" + id, ex);
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.PROCESSING, "Batch submission interrupted");
            } catch (ExecutionException ee) {
                log.error("unexpected exception during batch processing", ee);
                failedIds.add("unknown-error");
            }
        }

        return new PaymentBatchResult(distinct.size(), succeeded.get(), 0, failedIds.size(), new ArrayList<>(failedIds));
    }

    /**
     * 风控审核通过后的状态更新。
     */
    @Transactional
    public PaymentInstruction riskApprove(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
        if (instruction.getStatus() == PaymentStatus.RISK_APPROVED) {
            return instruction;
        }
        boolean updated = repository.compareAndUpdateStatus(instructionId, PaymentStatus.IN_RISK_REVIEW, PaymentStatus.RISK_APPROVED);
        if (!updated) {
            throw new BusinessException(ErrorCode.PROCESSING, "Instruction status changed, cannot approve");
        }
        instruction.setStatus(PaymentStatus.RISK_APPROVED);
        return instruction;
    }

    /**
     * 账务入账后置成功状态。
     */
    @Transactional
    public PaymentInstruction post(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
        if (instruction.getStatus() == PaymentStatus.POSTED) {
            return instruction;
        }
        boolean updated = repository.compareAndUpdateStatus(instructionId, PaymentStatus.RISK_APPROVED, PaymentStatus.POSTED)
                || repository.compareAndUpdateStatus(instructionId, PaymentStatus.CLEARING, PaymentStatus.POSTED);
        if (!updated) {
            throw new BusinessException(ErrorCode.PROCESSING, "Instruction not ready for posting");
        }
        instruction.setStatus(PaymentStatus.POSTED);
        return instruction;
    }

    /**
     * 处理失败场景，标记失败供上游查看。
     */
    @Transactional
    public PaymentInstruction fail(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
        if (instruction.getStatus() == PaymentStatus.FAILED) {
            return instruction;
        }
        boolean updated = repository.compareAndUpdateStatus(instructionId, instruction.getStatus(), PaymentStatus.FAILED);
        if (!updated) {
            throw new BusinessException(ErrorCode.PROCESSING, "Instruction status changed, cannot mark failed");
        }
        instruction.setStatus(PaymentStatus.FAILED);
        return instruction;
    }

    /**
     * 查询单条支付指令。
     */
    public PaymentInstruction get(String instructionId) {
        return repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
    }

    /**
     * 查询全部支付指令列表。
     */
    public List<PaymentInstruction> list() {
        return repository.findAll();
    }

    /**
     * 已存在的请求处理：复用成功结果，或告知正在处理/失败原因。
     */
    private PaymentInstruction handleExistingRequest(PaymentRequestRecord existing) {
        if (existing.getStatus() == PaymentRequestStatus.SUCCEEDED && existing.getPaymentInstructionId() != null) {
            return repository.findById(existing.getPaymentInstructionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                            "Payment previously succeeded but record missing"));
        }
        if (existing.getStatus() == PaymentRequestStatus.PENDING || existing.getStatus() == PaymentRequestStatus.PROCESSING) {
            if (existing.getPaymentInstructionId() != null) {
                return repository.findById(existing.getPaymentInstructionId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Processing payment not found"));
            }
            throw new BusinessException(ErrorCode.PROCESSING, "Payment request is already being processed");
        }
        if (existing.getStatus() == PaymentRequestStatus.FAILED) {
            String reason = existing.getMessage() == null ? "" : existing.getMessage();
            throw new BusinessException(ErrorCode.FAILED, "Previous attempt failed: " + reason);
        }
        throw new BusinessException(ErrorCode.PROCESSING, "Payment request is already being processed");
    }

    /**
     * 请求锁被占用时兜底查询：确保重复提交直接返回已有处理结果。
     */
    private PaymentInstruction resolveExistingRequest(String requestId) {
        PaymentRequestRecord existing = requestRepository.findByRequestId(requestId).orElse(null);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PROCESSING, "Payment request is being created, please retry later");
        }
        return handleExistingRequest(existing);
    }
}
