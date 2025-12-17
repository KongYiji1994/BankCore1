package com.bankcore.payment.client;

import com.bankcore.common.dto.AccountDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AccountClient {
    private final WebClient webClient;
    private final String accountServiceBaseUrl;

    public AccountClient(WebClient webClient,
                         @Value("${account-service.url:http://localhost:8081}") String accountServiceBaseUrl) {
        this.webClient = webClient;
        this.accountServiceBaseUrl = accountServiceBaseUrl;
    }

    public AccountDTO getAccount(String accountId) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    public AccountDTO freeze(String accountId, java.math.BigDecimal amount, String requestId) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/freeze?amount=" + amount + "&requestId=" + requestId;
        return webClient.post()
                .uri(url)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    public AccountDTO unfreeze(String accountId, java.math.BigDecimal amount, String requestId) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/unfreeze?amount=" + amount + "&requestId=" + requestId;
        return webClient.post()
                .uri(url)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    public AccountDTO settle(String accountId, java.math.BigDecimal amount, String requestId) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/settle?amount=" + amount + "&requestId=" + requestId;
        return webClient.post()
                .uri(url)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }
}
