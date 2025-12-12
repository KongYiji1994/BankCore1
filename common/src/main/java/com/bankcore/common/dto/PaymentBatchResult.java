package com.bankcore.common.dto;

import java.util.Collections;
import java.util.List;

public class PaymentBatchResult {
    private int total;
    private int succeeded;
    private int rejected;
    private int failed;
    private List<String> failedIds;

    public PaymentBatchResult() {
        this.failedIds = Collections.emptyList();
    }

    public PaymentBatchResult(int total, int succeeded, int rejected, int failed, List<String> failedIds) {
        this.total = total;
        this.succeeded = succeeded;
        this.rejected = rejected;
        this.failed = failed;
        this.failedIds = failedIds;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(int succeeded) {
        this.succeeded = succeeded;
    }

    public int getRejected() {
        return rejected;
    }

    public void setRejected(int rejected) {
        this.rejected = rejected;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<String> getFailedIds() {
        return failedIds;
    }

    public void setFailedIds(List<String> failedIds) {
        this.failedIds = failedIds;
    }
}
