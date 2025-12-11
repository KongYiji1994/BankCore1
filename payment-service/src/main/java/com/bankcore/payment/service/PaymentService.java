package com.bankcore.payment.service;

import com.bankcore.common.dto.PaymentBatchResult;
import com.bankcore.common.dto.PaymentRequest;
import com.bankcore.common.dto.PaymentStatus;
import com.bankcore.payment.model.PaymentInstruction;
import com.bankcore.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository repository;
    private final PaymentRiskAssessor riskAssessor;
    private final PaymentClearingAdapter clearingAdapter;
    private final TaskExecutor paymentTaskExecutor;

    public PaymentService(PaymentRepository repository,
                          PaymentRiskAssessor riskAssessor,
                          PaymentClearingAdapter clearingAdapter,
                          @Qualifier("paymentTaskExecutor") TaskExecutor paymentTaskExecutor) {
        this.repository = repository;
        this.riskAssessor = riskAssessor;
        this.clearingAdapter = clearingAdapter;
        this.paymentTaskExecutor = paymentTaskExecutor;
    }

    @Transactional
    public PaymentInstruction submit(PaymentRequest request) {
        PaymentInstruction instruction = new PaymentInstruction(
                request.getInstructionId(),
                request.getPayerAccount(),
                request.getPayeeAccount(),
                request.getCurrency(),
                request.getAmount(),
                request.getPurpose(),
                request.getChannel(),
                request.getBatchId(),
                request.getPriority());
        repository.save(instruction);
        return instruction;
    }

    public CompletableFuture<PaymentInstruction> processAsync(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
        repository.updateStatus(instructionId, PaymentStatus.IN_RISK_REVIEW);
        instruction.setStatus(PaymentStatus.IN_RISK_REVIEW);

        CompletableFuture<PaymentInstruction> riskFuture = CompletableFuture.supplyAsync(() -> {
            BigDecimal score = riskAssessor.evaluate(instruction);
            PaymentStatus nextStatus = score.compareTo(BigDecimal.valueOf(80)) >= 0
                    ? PaymentStatus.RISK_REJECTED
                    : PaymentStatus.RISK_APPROVED;
            repository.updateRisk(instructionId, score, nextStatus);
            instruction.setRiskScore(score);
            instruction.setStatus(nextStatus);
            return instruction;
        }, paymentTaskExecutor);

        return riskFuture.thenCompose(result -> {
            if (result.getStatus() == PaymentStatus.RISK_REJECTED) {
                return CompletableFuture.completedFuture(result);
            }
            return CompletableFuture.supplyAsync(() -> {
                PaymentStatus clearingStatus = clearingAdapter.dispatch(result);
                if (clearingStatus == PaymentStatus.POSTED) {
                    repository.updateStatus(instructionId, PaymentStatus.POSTED);
                } else if (clearingStatus == PaymentStatus.CLEARING) {
                    repository.updateStatus(instructionId, PaymentStatus.CLEARING);
                } else {
                    repository.updateStatus(instructionId, PaymentStatus.FAILED);
                }
                return repository.findById(instructionId).orElse(result);
            }, paymentTaskExecutor);
        }).exceptionally(ex -> {
            repository.updateStatus(instructionId, PaymentStatus.FAILED);
            return repository.findById(instructionId).orElse(instruction);
        });
    }

    public PaymentBatchResult processBatch(List<String> instructionIds) {
        if (instructionIds == null || instructionIds.isEmpty()) {
            return new PaymentBatchResult(0, 0, 0, 0, Collections.emptyList());
        }
        List<CompletableFuture<PaymentInstruction>> futures = instructionIds.stream()
                .distinct()
                .map(this::processAsync)
                .collect(Collectors.toList());

        List<PaymentInstruction> processed = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        int rejected = (int) processed.stream().filter(p -> p.getStatus() == PaymentStatus.RISK_REJECTED).count();
        int failed = (int) processed.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count();
        int succeeded = processed.size() - rejected - failed;
        List<String> failedIds = processed.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .map(PaymentInstruction::getInstructionId)
                .collect(Collectors.toList());

        return new PaymentBatchResult(processed.size(), succeeded, rejected, failed, failedIds);
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
