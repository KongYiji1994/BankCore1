package com.bankcore.payment.service;

import com.bankcore.payment.model.PaymentInstruction;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentRepository {
    private final Map<String, PaymentInstruction> storage = new ConcurrentHashMap<>();

    public void save(PaymentInstruction instruction) {
        storage.put(instruction.getInstructionId(), instruction);
    }

    public Optional<PaymentInstruction> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Map<String, PaymentInstruction> findAll() {
        return storage;
    }
}
