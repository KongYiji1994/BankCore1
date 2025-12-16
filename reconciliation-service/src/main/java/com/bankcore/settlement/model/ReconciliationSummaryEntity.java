package com.bankcore.settlement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReconciliationSummaryEntity {
    /** 汇总记录主键 */
    private Long id;
    /** 对账文件名 */
    private String fileName;
    /** 记录总数 */
    private int totalCount;
    /** 匹配成功数量 */
    private int matchedCount;
    /** 仅内部存在数量 */
    private int internalOnlyCount;
    /** 仅外部存在数量 */
    private int externalOnlyCount;
    /** 金额不符数量 */
    private int amountMismatchCount;
    /** 对账日期 */
    private LocalDate reconDate;
    /** 创建时间 */
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
