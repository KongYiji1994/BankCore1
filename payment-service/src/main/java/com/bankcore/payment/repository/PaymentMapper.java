package com.bankcore.payment.repository;

import com.bankcore.payment.model.PaymentInstruction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaymentMapper {
    void insert(PaymentInstruction instruction);

    PaymentInstruction findById(@Param("instructionId") String instructionId);

    List<PaymentInstruction> findAll();

    void updateStatus(@Param("instructionId") String instructionId, @Param("status") String status);

    void updateRisk(@Param("instructionId") String instructionId,
                    @Param("riskScore") java.math.BigDecimal riskScore,
                    @Param("status") String status);
}
