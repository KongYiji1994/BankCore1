package com.bankcore.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountDTO(@NotBlank String accountId,
                         @NotBlank String customerId,
                         @NotBlank String currency,
                         @NotNull @Min(0) BigDecimal balance,
                         @NotNull @Min(0) BigDecimal availableBalance) {
}
