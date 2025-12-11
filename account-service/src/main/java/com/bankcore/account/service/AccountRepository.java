package com.bankcore.account.service;

import com.bankcore.account.model.Account;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AccountRepository {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public Optional<Account> findById(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public void save(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public Collection<Account> findAll() {
        return accounts.values();
    }
}
