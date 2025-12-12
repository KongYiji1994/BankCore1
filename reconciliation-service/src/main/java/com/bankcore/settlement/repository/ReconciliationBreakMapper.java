package com.bankcore.settlement.repository;

import com.bankcore.settlement.model.ReconciliationBreak;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReconciliationBreakMapper {
    void insertBreaks(List<ReconciliationBreak> breaks);

    List<ReconciliationBreak> findBySummaryId(@Param("summaryId") Long summaryId);
}
