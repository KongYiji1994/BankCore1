package com.bankcore.settlement.repository;

import com.bankcore.settlement.model.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PaymentMapper {
    List<PaymentRecord> findPaymentsForDate(@Param("reconDate") LocalDate reconDate);
}
