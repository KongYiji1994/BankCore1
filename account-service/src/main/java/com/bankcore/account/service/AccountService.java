package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import com.bankcore.account.model.AccountLedgerEntry;
import com.bankcore.account.repository.AccountLedgerRepository;
import com.bankcore.account.repository.AccountRepository;
import com.bankcore.common.dto.AccountDTO;
import com.bankcore.common.error.BusinessException;
import com.bankcore.common.error.ErrorCode;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账户核心服务，负责账户开户、收付款记账、冻结与解冻、关闭等全生命周期操作。
 * 所有方法均带事务控制，确保余额字段(total/available/frozen)的一致性。
 */
@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private static final long ACCOUNT_LOCK_TTL_SECONDS = 30L;
    private final AccountRepository repository;
    private final AccountDomainSupport domainSupport;
    private final AccountLedgerRepository ledgerRepository;
    private final AccountLockManager lockManager;
    private final Map<AccountOperationType, OperationDefinition> operations;

    /**
     * 构造函数注入仓储接口，便于单元测试与替换实现。
     */
    public AccountService(AccountRepository repository,
                          AccountDomainSupport domainSupport,
                          AccountLedgerRepository ledgerRepository,
                          AccountLockManager lockManager) {
        this.repository = repository;
        this.domainSupport = domainSupport;
        this.ledgerRepository = ledgerRepository;
        this.lockManager = lockManager;
        this.operations = Collections.unmodifiableMap(initOperations());
    }

    /**
     * 开户：生成账户号，初始化三余额，并落库。
     */
    @Transactional
    public AccountDTO createAccount(String customerId, String currency, BigDecimal openingBalance) {
        String accountId = UUID.randomUUID().toString();
        Account account = new Account(accountId, customerId, currency, openingBalance);
        repository.save(account);
        log.info("created account {} for customer {} with currency {}", accountId, customerId, currency);
        return domainSupport.toDto(account);
    }

    /**
     * 收款入账：增加总余额与可用余额。
     */
    @Transactional
    public AccountDTO credit(String accountId, BigDecimal amount, String requestId) {
        return executeOperation(AccountOperationType.CREDIT, accountId, amount, requestId);
    }

    /**
     * 支付前冻结：先扣减可用余额、增加冻结金额，防止超扣。
     */
    @Transactional
    public AccountDTO freezeAmount(String accountId, BigDecimal amount, String requestId) {
        return executeOperation(AccountOperationType.FREEZE, accountId, amount, requestId);
    }

    /**
     * 清算成功：减少总余额与冻结金额，完成出账。
     */
    @Transactional
    public AccountDTO settle(String accountId, BigDecimal amount, String requestId) {
        return executeOperation(AccountOperationType.SETTLE, accountId, amount, requestId);
    }

    /**
     * 取消支付或失败补偿：释放冻结金额、恢复可用余额。
     */
    @Transactional
    public AccountDTO unfreeze(String accountId, BigDecimal amount, String requestId) {
        return executeOperation(AccountOperationType.UNFREEZE, accountId, amount, requestId);
    }

    /**
     * 关闭账户：仅余额为零时允许关闭。
     */
    @Transactional
    public AccountDTO close(String accountId) {
        return executeOperation(AccountOperationType.CLOSE, accountId, BigDecimal.ZERO, null);
    }

    /**
     * 按账户号查询账户。
     */
    public AccountDTO get(String accountId) {
        return domainSupport.toDto(domainSupport.findAccount(accountId, true));
    }

    /**
     * 查询所有账户列表。
     */
    public List<AccountDTO> list() {
        return repository.findAll().stream().map(domainSupport::toDto).collect(Collectors.toList());
    }

    /**
     * 查询指定客户下的所有账户。
     */
    public List<AccountDTO> listByCustomer(String customerId) {
        return repository.findByCustomer(customerId).stream().map(domainSupport::toDto).collect(Collectors.toList());
    }

    private AccountDTO executeOperation(AccountOperationType operationType, String accountId, BigDecimal amount, String requestId) {
        OperationDefinition selected = Optional.ofNullable(operations.get(operationType)).orElseThrow(
                () -> new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Unsupported account operation: " + operationType));
        String normalizedRequestId = normalizeRequestId(requestId);
        if (selected.requiresIdempotency()) {
            Optional<AccountLedgerEntry> existing = ledgerRepository.findByRequestId(normalizedRequestId);
            if (existing.isPresent()) {
                log.info("ledger request {} for account {} already processed, skip duplicate {}", normalizedRequestId, accountId,
                        operationType);
                return domainSupport.toDto(domainSupport.findAccount(accountId, true));
            }
        }

        return executeWithLock(accountId, account -> {
            selected.handler().apply(account, amount);
            repository.update(account);
            AccountDTO dto = domainSupport.toDto(account);
            if (selected.shouldRecordLedger()) {
                saveLedger(normalizedRequestId, accountId, amount, account, operationType);
            }
            selected.afterCommit().accept(accountId, amount, normalizedRequestId);
            return dto;
        });
    }

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

    private void saveLedger(String requestId, String accountId, BigDecimal amount, Account account, AccountOperationType operationType) {
        AccountLedgerEntry entry = new AccountLedgerEntry(
                UUID.randomUUID().toString(),
                requestId,
                accountId,
                operationType.name(),
                amount,
                account.getTotalBalance(),
                account.getAvailableBalance(),
                account.getFrozenBalance());
        ledgerRepository.save(entry);
    }

    private Map<AccountOperationType, OperationDefinition> initOperations() {
        EnumMap<AccountOperationType, OperationDefinition> map = new EnumMap<>(AccountOperationType.class);
        map.put(AccountOperationType.CREDIT, new OperationDefinition(Account::credit, true, true,
                (accountId, amount, requestId) -> log.info("credited account {} amount {}", accountId, amount)));
        map.put(AccountOperationType.FREEZE, new OperationDefinition(Account::prepareDebit, true, true,
                (accountId, amount, requestId) -> log.info("frozen amount {} on account {}", amount, accountId)));
        map.put(AccountOperationType.SETTLE, new OperationDefinition(Account::settleDebit, true, true,
                (accountId, amount, requestId) -> log.info("settled debit amount {} on account {}", amount, accountId)));
        map.put(AccountOperationType.UNFREEZE, new OperationDefinition(Account::releaseFrozen, true, true,
                (accountId, amount, requestId) -> log.info("unfroze amount {} on account {}", amount, accountId)));
        map.put(AccountOperationType.CLOSE, new OperationDefinition((account, amt) -> account.close(), false, false,
                (accountId, amount, requestId) -> log.info("closed account {}", accountId)));
        return map;
    }

    @FunctionalInterface
    private interface AccountOperationHandler {
        void apply(Account account, BigDecimal amount);
    }

    @FunctionalInterface
    private interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    private record OperationDefinition(AccountOperationHandler handler,
                                       boolean requiresIdempotency,
                                       boolean shouldRecordLedger,
                                       TriConsumer<String, BigDecimal, String> afterCommit) {
        OperationDefinition {
            if (handler == null) {
                throw new IllegalArgumentException("Account operation handler is required");
            }
            afterCommit = afterCommit == null ? (id, amount, request) -> { } : afterCommit;
        }
    }
}
