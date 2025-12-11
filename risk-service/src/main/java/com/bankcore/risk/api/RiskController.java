package com.bankcore.risk.api;

import com.bankcore.risk.model.RiskDecision;
import com.bankcore.risk.service.RiskEngineService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@RestController
@RequestMapping("/risk")
public class RiskController {
    private final RiskEngineService riskEngineService;

    public RiskController(RiskEngineService riskEngineService) {
        this.riskEngineService = riskEngineService;
    }

    @PostMapping("/evaluate")
    public RiskDecision evaluate(@Valid @RequestBody RiskRequest request) {
        return riskEngineService.evaluate(request);
    }

    public static class RiskRequest {
        @DecimalMin("0.01")
        private BigDecimal amount;
        @NotBlank
        private String customerId;
        @NotBlank
        private String channel;
        @NotBlank
        private String payerAccount;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getPayerAccount() {
            return payerAccount;
        }

        public void setPayerAccount(String payerAccount) {
            this.payerAccount = payerAccount;
        }
    }
}
