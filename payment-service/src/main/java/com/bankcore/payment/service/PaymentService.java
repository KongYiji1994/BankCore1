package com.bankcore.payment.service;

import com.bankcore.common.dto.AccountDTO;
import com.bankcore.common.dto.PaymentBatchResult;
import com.bankcore.common.dto.PaymentRequest;
import com.bankcore.common.dto.PaymentStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository repository;
    private final PaymentRequestRepository requestRepository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(PaymentRepository repository,
                          PaymentRequestRepository requestRepository,
                          AccountClient accountClient,
                          CustomerClient customerClient,
                          PaymentEventPublisher paymentEventPublisher) {
        this.repository = repository;
        this.requestRepository = requestRepository;
        this.accountClient = accountClient;
        this.customerClient = customerClient;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Transactional
    public PaymentInstruction submit(PaymentRequest request) {
        PaymentRequestRecord existing = requestRepository.findByRequestId(request.getRequestId()).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == PaymentRequestStatus.SUCCEEDED && existing.getPaymentInstructionId() != null) {
                return repository.findById(existing.getPaymentInstructionId())
                        .orElseThrow(() -> new IllegalStateException("Payment previously succeeded but record missing"));
            }
            if (existing.getStatus() == PaymentRequestStatus.PENDING || existing.getStatus() == PaymentRequestStatus.PROCESSING) {
                if (existing.getPaymentInstructionId() != null) {
                    return repository.findById(existing.getPaymentInstructionId())
                            .orElseThrow(() -> new IllegalStateException("Processing payment not found"));
                }
                throw new IllegalStateException("Payment request is already being processed");
            }
        }
        AccountDTO payerAccount = accountClient.getAccount(request.getPayerAccount());
        if (payerAccount == null) {
            throw new IllegalArgumentException("Payer account not found");
        }
        CustomerClient.CustomerProfile payerCustomer = customerClient.getCustomer(payerAccount.getCustomerId());
        if (payerCustomer == null) {
            throw new IllegalArgumentException("Customer not found for account");
        }
        String payerStatus = payerCustomer.getStatus() == null ? "NORMAL" : payerCustomer.getStatus();
        if ("BLOCKED".equalsIgnoreCase(payerStatus)) {
            throw new IllegalStateException("Payer customer is blocked");
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
        repository.save(instruction);
        if (existing == null) {
            requestRepository.createPending(request.getRequestId(), instruction.getInstructionId());
        } else {
            requestRepository.updateStatus(request.getRequestId(), PaymentRequestStatus.PENDING, instruction.getInstructionId(), "replay after failure");
        }
        paymentEventPublisher.publishAsync(request.getRequestId(), instruction.getInstructionId());
        return instruction;
    }

    public PaymentInstruction enqueueForProcessing(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
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
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
        instruction.setStatus(PaymentStatus.RISK_APPROVED);
        repository.updateStatus(instructionId, PaymentStatus.RISK_APPROVED);
        return instruction;
    }

    @Transactional
    public PaymentInstruction post(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
        instruction.setStatus(PaymentStatus.POSTED);
        repository.updateStatus(instructionId, PaymentStatus.POSTED);
        return instruction;
    }

    @Transactional
    public PaymentInstruction fail(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
        instruction.setStatus(PaymentStatus.FAILED);
        repository.updateStatus(instructionId, PaymentStatus.FAILED);
        return instruction;
    }

    public PaymentInstruction get(String instructionId) {
        return repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
    }

    public List<PaymentInstruction> list() {
        return repository.findAll();
    }
}
