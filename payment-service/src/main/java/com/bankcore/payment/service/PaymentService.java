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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PaymentService(PaymentRepository repository,
                          PaymentRequestRepository requestRepository,
                          AccountClient accountClient,
                          CustomerClient customerClient,
                          PaymentEventPublisher paymentEventPublisher,
                          PaymentIdempotencyManager idempotencyManager) {
        this.repository = repository;
        this.requestRepository = requestRepository;
        this.accountClient = accountClient;
        this.customerClient = customerClient;
        this.paymentEventPublisher = paymentEventPublisher;
        this.idempotencyManager = idempotencyManager;
    }

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

    public PaymentInstruction enqueueForProcessing(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
        log.info("manual enqueue instructionId={}, requestId={}", instructionId, instruction.getRequestId());
        requestRepository.updateStatus(instruction.getRequestId(), PaymentRequestStatus.PENDING, instructionId, "manual enqueue");
        paymentEventPublisher.publishAsync(instruction.getRequestId(), instructionId);
        return instruction;
    }

    public PaymentBatchResult processBatch(List<String> instructionIds) {
        if (instructionIds == null || instructionIds.isEmpty()) {
            return new PaymentBatchResult(0, 0, 0, 0, Collections.emptyList());
        }
        List<String> distinct = instructionIds.stream().distinct().collect(Collectors.toList());
        for (String id : distinct) {
            enqueueForProcessing(id);
        }
        return new PaymentBatchResult(distinct.size(), 0, 0, 0, Collections.emptyList());
    }

    @Transactional
    public PaymentInstruction riskApprove(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
        instruction.setStatus(PaymentStatus.RISK_APPROVED);
        repository.updateStatus(instructionId, PaymentStatus.RISK_APPROVED);
        return instruction;
    }

    @Transactional
    public PaymentInstruction post(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
        instruction.setStatus(PaymentStatus.POSTED);
        repository.updateStatus(instructionId, PaymentStatus.POSTED);
        return instruction;
    }

    @Transactional
    public PaymentInstruction fail(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
        instruction.setStatus(PaymentStatus.FAILED);
        repository.updateStatus(instructionId, PaymentStatus.FAILED);
        return instruction;
    }

    public PaymentInstruction get(String instructionId) {
        return repository.findById(instructionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Instruction not found"));
    }

    public List<PaymentInstruction> list() {
        return repository.findAll();
    }

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
            throw new BusinessException(ErrorCode.FAILED, "Previous attempt failed: " + existing.getStatusReason());
        }
        throw new BusinessException(ErrorCode.PROCESSING, "Payment request is already being processed");
    }

    private PaymentInstruction resolveExistingRequest(String requestId) {
        PaymentRequestRecord existing = requestRepository.findByRequestId(requestId).orElse(null);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PROCESSING, "Payment request is being created, please retry later");
        }
        return handleExistingRequest(existing);
    }
}
