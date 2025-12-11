package com.bankcore.settlement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReconciliationSummaryEntity {
    private Long id;
    private String fileName;
    private int totalCount;
    private int matchedCount;
    private int internalOnlyCount;
    private int externalOnlyCount;
    private int amountMismatchCount;
    private LocalDate reconDate;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public int getInternalOnlyCount() {
        return internalOnlyCount;
    }

    public void setInternalOnlyCount(int internalOnlyCount) {
        this.internalOnlyCount = internalOnlyCount;
    }

    public int getExternalOnlyCount() {
        return externalOnlyCount;
    }

    public void setExternalOnlyCount(int externalOnlyCount) {
        this.externalOnlyCount = externalOnlyCount;
    }

    public int getAmountMismatchCount() {
        return amountMismatchCount;
    }

    public void setAmountMismatchCount(int amountMismatchCount) {
        this.amountMismatchCount = amountMismatchCount;
    }

    public LocalDate getReconDate() {
        return reconDate;
    }

    public void setReconDate(LocalDate reconDate) {
        this.reconDate = reconDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
