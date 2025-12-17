package com.bankcore.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CustomerClient {
    private final WebClient webClient;
    private final String customerServiceBaseUrl;

    public CustomerClient(WebClient webClient,
                          @Value("${customer-service.url:http://localhost:8082}") String customerServiceBaseUrl) {
        this.webClient = webClient;
        this.customerServiceBaseUrl = customerServiceBaseUrl;
    }

    public CustomerProfile getCustomer(String customerId) {
        String url = customerServiceBaseUrl + "/customers/" + customerId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(CustomerProfile.class)
                .block();
    }

    public static class CustomerProfile {
        private String customerId;
        private String name;
        private String riskLevel;
        private String status;

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
