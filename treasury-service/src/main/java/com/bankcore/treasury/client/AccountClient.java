package com.bankcore.treasury.client;

import com.bankcore.common.dto.AccountDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

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

    public AccountDTO credit(String accountId, BigDecimal amount) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/credit?amount=" + amount;
        return webClient.post()
                .uri(url)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    public AccountDTO debit(String accountId, BigDecimal amount) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/debit?amount=" + amount;
        return webClient.post()
                .uri(url)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    public AccountDTO settle(String accountId, BigDecimal amount) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/settle?amount=" + amount;
        return webClient.post()
                .uri(url)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }
}
