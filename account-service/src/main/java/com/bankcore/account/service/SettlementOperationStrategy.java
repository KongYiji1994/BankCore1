package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SettlementOperationStrategy implements AccountOperationStrategy {
    private static final Logger log = LoggerFactory.getLogger(SettlementOperationStrategy.class);
    public static final AccountOperationType OPERATION_TYPE = AccountOperationType.SETTLE;

    @Override
    public AccountOperationType operationType() {
        return OPERATION_TYPE;
    }

    @Override
    public void applyOperation(Account account, BigDecimal amount) {
        account.settleDebit(amount);
    }

    @Override
    public void afterCommit(String accountId, BigDecimal amount, String requestId) {
        log.info("settled debit amount {} on account {}", amount, accountId);
    }
}
