package com.bankcore.treasury.client;

import com.bankcore.common.dto.AccountDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class AccountClient {
    private final RestTemplate restTemplate;
    private final String accountServiceBaseUrl;

    public AccountClient(RestTemplate restTemplate,
                         @Value("${account-service.url:http://localhost:8081}") String accountServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceBaseUrl = accountServiceBaseUrl;
    }

    public AccountDTO getAccount(String accountId) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId;
        return restTemplate.getForObject(url, AccountDTO.class);
    }

    public AccountDTO credit(String accountId, BigDecimal amount) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/credit?amount=" + amount;
        return restTemplate.postForObject(url, null, AccountDTO.class);
    }

    public AccountDTO debit(String accountId, BigDecimal amount) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/debit?amount=" + amount;
        return restTemplate.postForObject(url, null, AccountDTO.class);
    }

    public AccountDTO settle(String accountId, BigDecimal amount) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/settle?amount=" + amount;
        return restTemplate.postForObject(url, null, AccountDTO.class);
    }
}
