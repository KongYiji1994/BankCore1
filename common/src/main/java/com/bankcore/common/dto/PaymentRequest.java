package com.bankcore.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(@NotBlank String instructionId,
                             @NotBlank String payerAccount,
                             @NotBlank String payeeAccount,
                             @NotBlank String currency,
                             @NotNull @Min(1) BigDecimal amount,
                             @NotBlank String purpose) {
}
