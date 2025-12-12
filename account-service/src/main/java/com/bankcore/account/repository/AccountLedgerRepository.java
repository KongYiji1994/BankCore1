package com.bankcore.account.repository;

import com.bankcore.account.model.AccountLedgerEntry;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AccountLedgerRepository {
    private final AccountLedgerMapper mapper;

    public AccountLedgerRepository(AccountLedgerMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<AccountLedgerEntry> findByRequestId(String requestId) {
        return Optional.ofNullable(mapper.findByRequestId(requestId));
    }

    public void save(AccountLedgerEntry entry) {
        mapper.insert(entry);
    }
}
