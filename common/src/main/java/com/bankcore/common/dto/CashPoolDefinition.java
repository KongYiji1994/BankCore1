package com.bankcore.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Set;

public record CashPoolDefinition(@NotBlank String poolId,
                                 @NotBlank String headerAccount,
                                 @NotNull Set<String> memberAccounts,
                                 @NotNull BigDecimal targetBalance,
                                 @NotBlank String strategy) {
}
