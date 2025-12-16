package com.bankcore.settlement.model;

import java.util.List;

public class ReconciliationResult {
    /** 对账汇总结果 */
    private ReconciliationSummaryEntity summary;
    /** 差异明细列表 */
    private List<ReconciliationBreak> breaks;

    public ReconciliationSummaryEntity getSummary() {
        return summary;
    }

    public void setSummary(ReconciliationSummaryEntity summary) {
        this.summary = summary;
    }

    public List<ReconciliationBreak> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<ReconciliationBreak> breaks) {
        this.breaks = breaks;
    }
}
