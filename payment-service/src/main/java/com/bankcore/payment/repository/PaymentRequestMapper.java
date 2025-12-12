package com.bankcore.payment.repository;

import com.bankcore.payment.model.PaymentRequestRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentRequestMapper {

    void insert(PaymentRequestRecord record);

    PaymentRequestRecord findByRequestId(@Param("requestId") String requestId);

    void updateStatus(@Param("requestId") String requestId,
                      @Param("status") String status,
                      @Param("paymentInstructionId") String paymentInstructionId,
                      @Param("message") String message);

    int compareAndUpdateStatus(@Param("requestId") String requestId,
                               @Param("expectedStatus") String expectedStatus,
                               @Param("status") String status,
                               @Param("paymentInstructionId") String paymentInstructionId,
                               @Param("message") String message);
}
