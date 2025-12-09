package com.bankcore.payment.repository;

import com.bankcore.payment.model.PaymentInstruction;
import com.bankcore.common.dto.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository {
    private final PaymentMapper mapper;

    public PaymentRepository(PaymentMapper mapper) {
        this.mapper = mapper;
    }

    public void save(PaymentInstruction instruction) {
        mapper.insert(instruction);
    }

    public Optional<PaymentInstruction> findById(String instructionId) {
        return Optional.ofNullable(mapper.findById(instructionId));
    }

    public List<PaymentInstruction> findAll() {
        return mapper.findAll();
    }

    public void updateStatus(String instructionId, PaymentStatus status) {
        mapper.updateStatus(instructionId, status.name());
    }
}
