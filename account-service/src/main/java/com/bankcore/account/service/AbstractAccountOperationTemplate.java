package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import com.bankcore.account.model.AccountLedgerEntry;
import com.bankcore.account.repository.AccountLedgerRepository;
import com.bankcore.account.repository.AccountRepository;
import com.bankcore.common.dto.AccountDTO;
import com.bankcore.common.error.BusinessException;
import com.bankcore.common.error.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 账户操作模板，统一加锁、幂等校验与记账分录落库的处理流程。
 * 具体操作通过策略子类提供，便于拓展新的账户交易类型。
 */
public abstract class AbstractAccountOperationTemplate implements AccountOperationStrategy {
    private static final Logger log = LoggerFactory.getLogger(AbstractAccountOperationTemplate.class);
    private static final long ACCOUNT_LOCK_TTL_SECONDS = 30L;

    private final AccountRepository repository;
    private final AccountLedgerRepository ledgerRepository;
    private final AccountLockManager lockManager;
    private final AccountDomainSupport domainSupport;

    protected AbstractAccountOperationTemplate(AccountRepository repository,
                                               AccountLedgerRepository ledgerRepository,
                                               AccountLockManager lockManager,
                                               AccountDomainSupport domainSupport) {
        this.repository = repository;
        this.ledgerRepository = ledgerRepository;
        this.lockManager = lockManager;
        this.domainSupport = domainSupport;
    }

    @Override
    public final AccountDTO execute(String accountId, BigDecimal amount, String requestId) {
        String normalizedRequestId = normalizeRequestId(requestId);
        if (requiresIdempotency()) {
            Optional<AccountLedgerEntry> existing = ledgerRepository.findByRequestId(normalizedRequestId);
            if (existing.isPresent()) {
                log.info("ledger request {} for account {} already processed, skip duplicate {}", normalizedRequestId, accountId,
                        operationType());
                return domainSupport.toDto(domainSupport.findAccount(accountId));
            }
        }

        return executeWithLock(accountId, account -> {
            applyOperation(account, amount);
            repository.update(account);
            AccountDTO dto = domainSupport.toDto(account);
            if (shouldRecordLedger()) {
                saveLedger(normalizedRequestId, accountId, amount, account);
            }
            afterCommit(accountId, amount, normalizedRequestId);
            return dto;
        });
    }

    protected boolean requiresIdempotency() {
        return true;
    }

    protected boolean shouldRecordLedger() {
        return true;
    }

    protected void afterCommit(String accountId, BigDecimal amount, String requestId) {
        // 默认无附加动作
    }

    protected abstract void applyOperation(Account account, BigDecimal amount);

    private <T> T executeWithLock(String accountId, Function<Account, T> callback) {
        boolean locked = lockManager.tryLock(accountId, ACCOUNT_LOCK_TTL_SECONDS);
        if (!locked) {
            throw new BusinessException(ErrorCode.PROCESSING, "账户正在处理并发交易，请稍后重试");
        }
        try {
            Account account = domainSupport.findAccount(accountId);
            return callback.apply(account);
        } finally {
            lockManager.unlock(accountId);
        }
    }

    private String normalizeRequestId(String requestId) {
        return (requestId == null || requestId.trim().isEmpty())
                ? UUID.randomUUID().toString()
                : requestId.trim();
    }

    private void saveLedger(String requestId, String accountId, BigDecimal amount, Account account) {
        AccountLedgerEntry entry = new AccountLedgerEntry(
                UUID.randomUUID().toString(),
                requestId,
                accountId,
                operationType().name(),
                amount,
                account.getTotalBalance(),
                account.getAvailableBalance(),
                account.getFrozenBalance());
        ledgerRepository.save(entry);
    }
}
