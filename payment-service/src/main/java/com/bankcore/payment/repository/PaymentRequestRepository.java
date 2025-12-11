package com.bankcore.payment.repository;

import com.bankcore.payment.model.PaymentRequestRecord;
import com.bankcore.payment.model.PaymentRequestStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRequestRepository {
    private final PaymentRequestMapper mapper;

    public PaymentRequestRepository(PaymentRequestMapper mapper) {
        this.mapper = mapper;
    }

    public void save(PaymentRequestRecord record) {
        mapper.insert(record);
    }

    public Optional<PaymentRequestRecord> findByRequestId(String requestId) {
        return Optional.ofNullable(mapper.findByRequestId(requestId));
    }

    public void updateStatus(String requestId, PaymentRequestStatus status, String paymentInstructionId, String message) {
        mapper.updateStatus(requestId, status.name(), paymentInstructionId, message);
    }

    public PaymentRequestRecord createPending(String requestId, String paymentInstructionId) {
        PaymentRequestRecord record = new PaymentRequestRecord(requestId, paymentInstructionId, PaymentRequestStatus.PENDING);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(record.getCreatedAt());
        mapper.insert(record);
        return record;
    }
}
