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
        Account account = findAccount(accountId);
        account.credit(amount);
        repository.update(account);
        return toDto(account);
    }

    @Transactional
    public AccountDTO freezeAmount(String accountId, BigDecimal amount) {
        Account account = findAccount(accountId);
        account.prepareDebit(amount);
        repository.update(account);
        return toDto(account);
    }

    @Transactional
    public AccountDTO settle(String accountId, BigDecimal amount) {
        Account account = findAccount(accountId);
        account.settleDebit(amount);
        repository.update(account);
        return toDto(account);
    }

    @Transactional
    public AccountDTO unfreeze(String accountId, BigDecimal amount) {
        Account account = findAccount(accountId);
        account.releaseFrozen(amount);
        repository.update(account);
        return toDto(account);
    }

    @Transactional
    public AccountDTO close(String accountId) {
        Account account = findAccount(accountId);
        account.close();
        repository.update(account);
        return toDto(account);
    }

    public AccountDTO get(String accountId) {
        return toDto(findAccount(accountId, true));
    }

    public List<AccountDTO> list() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<AccountDTO> listByCustomer(String customerId) {
        return repository.findByCustomer(customerId).stream().map(this::toDto).collect(Collectors.toList());
    }

    private Account findAccount(String accountId) {
        return findAccount(accountId, false);
    }

    private Account findAccount(String accountId, boolean includeClosed) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!includeClosed && "CLOSED".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalStateException("Account is closed");
        }
        return account;
    }

    private AccountDTO toDto(Account account) {
        return new AccountDTO(account.getAccountId(), account.getCustomerId(), account.getCurrency(), account.getTotalBalance(),
                account.getAvailableBalance(), account.getFrozenBalance(), account.getStatus());
    }
}
