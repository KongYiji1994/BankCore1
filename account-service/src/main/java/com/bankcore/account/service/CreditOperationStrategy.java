package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import com.bankcore.account.repository.AccountLedgerRepository;
import com.bankcore.account.repository.AccountRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreditOperationStrategy extends AbstractAccountOperationTemplate {
    private static final Logger log = LoggerFactory.getLogger(CreditOperationStrategy.class);
    public static final AccountOperationType OPERATION_TYPE = AccountOperationType.CREDIT;

    public CreditOperationStrategy(AccountRepository repository, AccountLedgerRepository ledgerRepository,
                                   AccountLockManager lockManager, AccountDomainSupport domainSupport) {
        super(repository, ledgerRepository, lockManager, domainSupport);
    }

    @Override
    public AccountOperationType operationType() {
        return OPERATION_TYPE;
    }

    @Override
    protected void applyOperation(Account account, BigDecimal amount) {
        account.credit(amount);
    }

    @Override
    protected void afterCommit(String accountId, BigDecimal amount, String requestId) {
        log.info("credited account {} amount {}", accountId, amount);
    }
}
