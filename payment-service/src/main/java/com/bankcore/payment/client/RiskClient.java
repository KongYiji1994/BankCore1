package com.bankcore.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class RiskClient {
    private final RestTemplate restTemplate;
    private final String riskServiceBaseUrl;

    public RiskClient(RestTemplate restTemplate,
                      @Value("${risk-service.url:http://localhost:8086}") String riskServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.riskServiceBaseUrl = riskServiceBaseUrl;
    }

    public RiskDecisionResponse evaluate(BigDecimal amount, String customerId, String channel) {
        RiskRequest payload = new RiskRequest(amount, customerId, channel);
        return restTemplate.postForObject(riskServiceBaseUrl + "/risk/evaluate", payload, RiskDecisionResponse.class);
    }

    public static class RiskRequest {
        private BigDecimal amount;
        private String customerId;
        private String channel;

        public RiskRequest() {
        }

        public RiskRequest(BigDecimal amount, String customerId, String channel) {
            this.amount = amount;
            this.customerId = customerId;
            this.channel = channel;
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
    }

    public static class RiskDecisionResponse {
        private String decisionId;
        private String level;
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
