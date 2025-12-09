package com.bankcore.payment.service;

import com.bankcore.common.dto.PaymentRequest;
import com.bankcore.common.dto.PaymentStatus;
import com.bankcore.payment.model.PaymentInstruction;
import com.bankcore.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PaymentInstruction submit(PaymentRequest request) {
        PaymentInstruction instruction = new PaymentInstruction(
                request.getInstructionId(),
                request.getPayerAccount(),
                request.getPayeeAccount(),
                request.getCurrency(),
                request.getAmount(),
                request.getPurpose());
        repository.save(instruction);
        return instruction;
    }

    @Transactional
    public PaymentInstruction riskApprove(String instructionId) {
        PaymentInstruction instruction = repository.findById(instructionId)
                .orElseThrow(() -> new IllegalArgumentException("Instruction not found"));
        instruction.setStatus(PaymentStatus.RISK_REVIEWED);
        repository.updateStatus(instructionId, PaymentStatus.RISK_REVIEWED);
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
