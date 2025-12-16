package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CloseAccountOperationStrategy implements AccountOperationStrategy {
    private static final Logger log = LoggerFactory.getLogger(CloseAccountOperationStrategy.class);
    public static final AccountOperationType OPERATION_TYPE = AccountOperationType.CLOSE;

    @Override
    public AccountOperationType operationType() {
        return OPERATION_TYPE;
    }

    @Override
    public boolean requiresIdempotency() {
        return false;
    }

    @Override
    public boolean shouldRecordLedger() {
        return false;
    }

    @Override
    public void applyOperation(Account account, BigDecimal amount) {
        account.close();
    }

    @Override
    public void afterCommit(String accountId, BigDecimal amount, String requestId) {
        log.info("closed account {}", accountId);
    }
}
