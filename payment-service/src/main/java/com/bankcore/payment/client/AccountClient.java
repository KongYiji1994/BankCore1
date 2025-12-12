package com.bankcore.payment.client;

import com.bankcore.common.dto.AccountDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public AccountDTO freeze(String accountId, java.math.BigDecimal amount, String requestId) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/freeze?amount=" + amount + "&requestId=" + requestId;
        return restTemplate.postForObject(url, null, AccountDTO.class);
    }

    public AccountDTO unfreeze(String accountId, java.math.BigDecimal amount, String requestId) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/unfreeze?amount=" + amount + "&requestId=" + requestId;
        return restTemplate.postForObject(url, null, AccountDTO.class);
    }

    public AccountDTO settle(String accountId, java.math.BigDecimal amount, String requestId) {
        String url = accountServiceBaseUrl + "/accounts/" + accountId + "/settle?amount=" + amount + "&requestId=" + requestId;
        return restTemplate.postForObject(url, null, AccountDTO.class);
    }
}
