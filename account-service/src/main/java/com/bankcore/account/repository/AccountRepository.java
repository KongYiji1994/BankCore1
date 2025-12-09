package com.bankcore.account.repository;

import com.bankcore.account.model.Account;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepository {
    private final AccountMapper mapper;

    public AccountRepository(AccountMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<Account> findById(String accountId) {
        return Optional.ofNullable(mapper.findById(accountId));
    }

    public List<Account> findAll() {
        return mapper.findAll();
    }

    public void save(Account account) {
        mapper.insert(account);
    }

    public void update(Account account) {
        mapper.update(account);
    }
}
