package com.bankcore.payment.service;

import com.bankcore.payment.model.PaymentInstruction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PaymentRiskAssessor {

    public BigDecimal evaluate(PaymentInstruction instruction) {
        BigDecimal score = BigDecimal.valueOf(10L);
        BigDecimal amountFactor = instruction.getAmount().divide(BigDecimal.valueOf(10000L), 2, RoundingMode.HALF_UP);
        score = score.add(amountFactor.multiply(BigDecimal.valueOf(5))); // large amounts add risk

        if (!"CNY".equalsIgnoreCase(instruction.getCurrency())) {
            score = score.add(BigDecimal.valueOf(15)); // cross-border or FX adds risk
        }

        if (instruction.getPurpose() != null && instruction.getPurpose().toLowerCase().contains("cash")) {
            score = score.add(BigDecimal.valueOf(10));
        }

        if (instruction.getPriority() != null && instruction.getPriority() < 3) {
            score = score.add(BigDecimal.valueOf(5));
        }

        if (score.compareTo(BigDecimal.valueOf(95)) > 0) {
            score = BigDecimal.valueOf(95);
        }
        return score;
    }
}
