package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import com.bankcore.account.repository.AccountLedgerRepository;
import com.bankcore.account.repository.AccountRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CloseAccountOperationStrategy extends AbstractAccountOperationTemplate {
    private static final Logger log = LoggerFactory.getLogger(CloseAccountOperationStrategy.class);
    public static final AccountOperationType OPERATION_TYPE = AccountOperationType.CLOSE;

    public CloseAccountOperationStrategy(AccountRepository repository, AccountLedgerRepository ledgerRepository,
                                         AccountLockManager lockManager, AccountDomainSupport domainSupport) {
        super(repository, ledgerRepository, lockManager, domainSupport);
    }

    @Override
    public AccountOperationType operationType() {
        return OPERATION_TYPE;
    }

    @Override
    protected boolean requiresIdempotency() {
        return false;
    }

    @Override
    protected boolean shouldRecordLedger() {
        return false;
    }

    @Override
    protected void applyOperation(Account account, BigDecimal amount) {
        account.close();
    }

    @Override
    protected void afterCommit(String accountId, BigDecimal amount, String requestId) {
        log.info("closed account {}", accountId);
    }
}
