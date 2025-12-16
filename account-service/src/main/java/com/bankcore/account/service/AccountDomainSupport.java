package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import com.bankcore.account.repository.AccountRepository;
import com.bankcore.common.dto.AccountDTO;
import com.bankcore.common.error.BusinessException;
import com.bankcore.common.error.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 账户领域通用支撑，封装账户查询与 DTO 转换等公共逻辑。
 */
@Component
public class AccountDomainSupport {
    private final AccountRepository repository;

    public AccountDomainSupport(AccountRepository repository) {
        this.repository = repository;
    }

    public Account findAccount(String accountId) {
        return findAccount(accountId, false);
    }

    public Account findAccount(String accountId, boolean includeClosed) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Account not found"));
        if (!includeClosed && "CLOSED".equalsIgnoreCase(account.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Account is closed");
        }
        return account;
    }

    public AccountDTO toDto(Account account) {
        return new AccountDTO(account.getAccountId(), account.getCustomerId(), account.getCurrency(), account.getTotalBalance(),
                account.getAvailableBalance(), account.getFrozenBalance(), account.getStatus());
    }
}
