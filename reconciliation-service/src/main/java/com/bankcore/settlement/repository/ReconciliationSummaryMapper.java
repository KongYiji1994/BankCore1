package com.bankcore.settlement.repository;

import com.bankcore.settlement.model.ReconciliationSummaryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReconciliationSummaryMapper {
    void insertSummary(ReconciliationSummaryEntity summary);

    ReconciliationSummaryEntity findLatest();

    ReconciliationSummaryEntity findById(@Param("id") Long id);

    List<ReconciliationSummaryEntity> findByDate(@Param("date") LocalDate date);
}
