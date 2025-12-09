package com.bankcore.common.dto;

import java.util.List;

public record ReconciliationSummary(int total,
                                    int matched,
                                    List<String> breaks) {
}
