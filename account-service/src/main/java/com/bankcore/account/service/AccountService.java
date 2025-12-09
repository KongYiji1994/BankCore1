package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import com.bankcore.account.repository.AccountRepository;
import com.bankcore.common.dto.AccountDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AccountDTO createAccount(String customerId, String currency, BigDecimal openingBalance) {
        String accountId = UUID.randomUUID().toString();
        Account account = new Account(accountId, customerId, currency, openingBalance);
        repository.save(account);
        return toDto(account);
    }

    @Transactional
    public AccountDTO credit(String accountId, BigDecimal amount) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.credit(amount);
        repository.update(account);
        return toDto(account);
    }

    @Transactional
    public AccountDTO debit(String accountId, BigDecimal amount) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.debit(amount);
        repository.update(account);
        return toDto(account);
    }

    public AccountDTO get(String accountId) {
        return repository.findById(accountId).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public List<AccountDTO> list() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private AccountDTO toDto(Account account) {
        return new AccountDTO(account.getAccountId(), account.getCustomerId(), account.getCurrency(), account.getBalance(), account.getAvailableBalance());
    }
}
