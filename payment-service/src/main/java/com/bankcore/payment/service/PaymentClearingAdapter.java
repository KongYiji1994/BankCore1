package com.bankcore.payment.service;

import com.bankcore.common.dto.PaymentStatus;
import com.bankcore.payment.model.PaymentInstruction;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PaymentClearingAdapter {

    public PaymentStatus dispatch(PaymentInstruction instruction) {
        BigDecimal amount = instruction.getAmount();
        if (amount.compareTo(BigDecimal.valueOf(300000)) > 0) {
            return PaymentStatus.FAILED;
        }

        if (instruction.getCurrency() != null && !"CNY".equalsIgnoreCase(instruction.getCurrency())
                && amount.compareTo(BigDecimal.valueOf(20000)) > 0) {
            return PaymentStatus.CLEARING; // FX routing requires extra step before posting
        }
        return PaymentStatus.POSTED;
    }
}
