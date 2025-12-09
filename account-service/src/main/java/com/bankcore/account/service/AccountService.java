package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import com.bankcore.common.dto.AccountDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository repository = new AccountRepository();

    public AccountDTO createAccount(String customerId, String currency, BigDecimal openingBalance) {
        String accountId = UUID.randomUUID().toString();
        Account account = new Account(accountId, customerId, currency, openingBalance);
        repository.save(account);
        return toDto(account);
    }

    public AccountDTO credit(String accountId, BigDecimal amount) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.credit(amount);
        return toDto(account);
    }

    public AccountDTO debit(String accountId, BigDecimal amount) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.debit(amount);
        return toDto(account);
    }

    public AccountDTO get(String accountId) {
        return repository.findById(accountId).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public List<AccountDTO> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    private AccountDTO toDto(Account account) {
        return new AccountDTO(account.getAccountId(), account.getCustomerId(), account.getCurrency(), account.getBalance(), account.getAvailableBalance());
    }
}
