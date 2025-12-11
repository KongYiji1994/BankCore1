package com.bankcore.settlement.repository;

import com.bankcore.settlement.model.ReconciliationSummaryEntity;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReconciliationSummaryMapper {
    void insertSummary(ReconciliationSummaryEntity summary);

    ReconciliationSummaryEntity findLatest();

    ReconciliationSummaryEntity findById(Long id);

    List<ReconciliationSummaryEntity> findByDate(LocalDate date);
}
