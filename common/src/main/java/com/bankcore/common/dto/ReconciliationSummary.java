package com.bankcore.common.dto;

import java.util.List;

public class ReconciliationSummary {
    private int total;
    private int matched;
    private List<String> breaks;

    public ReconciliationSummary() {
    }

    public ReconciliationSummary(int total, int matched, List<String> breaks) {
        this.total = total;
        this.matched = matched;
        this.breaks = breaks;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getMatched() {
        return matched;
    }

    public void setMatched(int matched) {
        this.matched = matched;
    }

    public List<String> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<String> breaks) {
        this.breaks = breaks;
    }
}
