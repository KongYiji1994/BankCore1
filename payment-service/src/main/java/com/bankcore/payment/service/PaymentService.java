package com.bankcore.payment.service;

import com.bankcore.common.dto.PaymentRequest;
import com.bankcore.common.dto.PaymentStatus;
import com.bankcore.payment.model.PaymentInstruction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository repository = new PaymentRepository();

    public PaymentInstruction submit(PaymentRequest request) {
        PaymentInstruction instruction = new PaymentInstruction(
                request.instructionId(),
                request.payerAccount(),
                request.payeeAccount(),
                request.currency(),
                request.amount(),
                request.purpose());
        repository.save(instruction);
        return instruction;
    }

    public PaymentInstruction riskApprove(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
        instruction.setStatus(PaymentStatus.RISK_REVIEWED);
        return instruction;
    }

    public PaymentInstruction post(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
        instruction.setStatus(PaymentStatus.POSTED);
        return instruction;
    }

    public PaymentInstruction fail(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
        instruction.setStatus(PaymentStatus.FAILED);
        return instruction;
    }

    public PaymentInstruction get(String instructionId) {
        return repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
    }

    public List<PaymentInstruction> list() {
        return repository.findAll().values().stream().toList();
    }
}
