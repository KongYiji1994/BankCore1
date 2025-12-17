package com.bankcore.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class RiskClient {
    private final WebClient webClient;
    private final String riskServiceBaseUrl;

    public RiskClient(WebClient webClient,
                      @Value("${risk-service.url:http://localhost:8086}") String riskServiceBaseUrl) {
        this.webClient = webClient;
        this.riskServiceBaseUrl = riskServiceBaseUrl;
    }

    public RiskDecisionResponse evaluate(BigDecimal amount, String customerId, String channel, String payerAccount, String requestId) {
        RiskRequest payload = new RiskRequest(amount, customerId, channel, payerAccount, requestId);
        return webClient.post()
                .uri(riskServiceBaseUrl + "/risk/evaluate")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(RiskDecisionResponse.class)
                .block();
    }

    public static class RiskRequest {
        private BigDecimal amount;
        private String customerId;
        private String channel;
        private String payerAccount;
        private String requestId;

        public RiskRequest() {
        }

        public RiskRequest(BigDecimal amount, String customerId, String channel, String payerAccount, String requestId) {
            this.amount = amount;
            this.customerId = customerId;
            this.channel = channel;
            this.payerAccount = payerAccount;
            this.requestId = requestId;
        }

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

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }
    }

    public static class RiskDecisionResponse {
        private String decisionId;
        private String level;
        private String result;
        private String ruleType;
        private Long ruleId;
        private String reason;
        private boolean blocked;

        public String getDecisionId() {
            return decisionId;
        }

        public void setDecisionId(String decisionId) {
            this.decisionId = decisionId;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getRuleType() {
            return ruleType;
        }

        public void setRuleType(String ruleType) {
            this.ruleType = ruleType;
        }

        public Long getRuleId() {
            return ruleId;
        }

        public void setRuleId(Long ruleId) {
            this.ruleId = ruleId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public void setBlocked(boolean blocked) {
            this.blocked = blocked;
        }
    }
}
