package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FreezeOperationStrategy implements AccountOperationStrategy {
    private static final Logger log = LoggerFactory.getLogger(FreezeOperationStrategy.class);
    public static final AccountOperationType OPERATION_TYPE = AccountOperationType.FREEZE;

    @Override
    public AccountOperationType operationType() {
        return OPERATION_TYPE;
    }

    @Override
    public void applyOperation(Account account, BigDecimal amount) {
        account.prepareDebit(amount);
    }

    @Override
    public void afterCommit(String accountId, BigDecimal amount, String requestId) {
        log.info("frozen amount {} on account {}", amount, accountId);
    }
}
