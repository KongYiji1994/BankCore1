package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import com.bankcore.account.repository.AccountRepository;
import com.bankcore.common.dto.AccountDTO;
import com.bankcore.common.error.BusinessException;
import com.bankcore.common.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 账户核心服务，负责账户开户、收付款记账、冻结与解冻、关闭等全生命周期操作。
 * 所有方法均带事务控制，确保余额字段(total/available/frozen)的一致性。
 */
@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository repository;
    private final AccountLockManager lockManager;

    /**
     * 锁过期时间（秒），防止线程异常退出导致锁长时间占用。
     */
    private static final long ACCOUNT_LOCK_TTL_SECONDS = 30L;

    /**
     * 构造函数注入仓储接口，便于单元测试与替换实现。
     */
    public AccountService(AccountRepository repository, AccountLockManager lockManager) {
        this.repository = repository;
        this.lockManager = lockManager;
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
        return toDto(account);
    }

    /**
     * 收款入账：增加总余额与可用余额。
     */
    @Transactional
    public AccountDTO credit(String accountId, BigDecimal amount) {
        return executeWithLock(accountId, new AccountOperation<AccountDTO>() {
            @Override
            public AccountDTO apply(Account account) {
                account.credit(amount);
                repository.update(account);
                log.info("credited account {} amount {}", accountId, amount);
                return toDto(account);
            }
        });
    }

    /**
     * 支付前冻结：先扣减可用余额、增加冻结金额，防止超扣。
     */
    @Transactional
    public AccountDTO freezeAmount(String accountId, BigDecimal amount) {
        return executeWithLock(accountId, new AccountOperation<AccountDTO>() {
            @Override
            public AccountDTO apply(Account account) {
                account.prepareDebit(amount);
                repository.update(account);
                log.info("frozen amount {} on account {}", amount, accountId);
                return toDto(account);
            }
        });
    }

    /**
     * 清算成功：减少总余额与冻结金额，完成出账。
     */
    @Transactional
    public AccountDTO settle(String accountId, BigDecimal amount) {
        return executeWithLock(accountId, new AccountOperation<AccountDTO>() {
            @Override
            public AccountDTO apply(Account account) {
                account.settleDebit(amount);
                repository.update(account);
                log.info("settled debit amount {} on account {}", amount, accountId);
                return toDto(account);
            }
        });
    }

    /**
     * 取消支付或失败补偿：释放冻结金额、恢复可用余额。
     */
    @Transactional
    public AccountDTO unfreeze(String accountId, BigDecimal amount) {
        return executeWithLock(accountId, new AccountOperation<AccountDTO>() {
            @Override
            public AccountDTO apply(Account account) {
                account.releaseFrozen(amount);
                repository.update(account);
                log.info("unfroze amount {} on account {}", amount, accountId);
                return toDto(account);
            }
        });
    }

    /**
     * 关闭账户：仅余额为零时允许关闭。
     */
    @Transactional
    public AccountDTO close(String accountId) {
        return executeWithLock(accountId, new AccountOperation<AccountDTO>() {
            @Override
            public AccountDTO apply(Account account) {
                account.close();
                repository.update(account);
                log.info("closed account {}", accountId);
                return toDto(account);
            }
        });
    }

    /**
     * 按账户号查询账户。
     */
    public AccountDTO get(String accountId) {
        return toDto(findAccount(accountId, true));
    }

    /**
     * 查询所有账户列表。
     */
    public List<AccountDTO> list() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 查询指定客户下的所有账户。
     */
    public List<AccountDTO> listByCustomer(String customerId) {
        return repository.findByCustomer(customerId).stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 获取有效账户，默认不包含已关闭账户。
     */
    private Account findAccount(String accountId) {
        return findAccount(accountId, false);
    }

    /**
     * 获取账户，可配置是否允许返回已关闭账户。
     */
    private Account findAccount(String accountId, boolean includeClosed) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Account not found"));
        if (!includeClosed && "CLOSED".equalsIgnoreCase(account.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Account is closed");
        }
        return account;
    }

    /**
     * 领域对象转 DTO，供外部接口返回。
     */
    private AccountDTO toDto(Account account) {
        return new AccountDTO(account.getAccountId(), account.getCustomerId(), account.getCurrency(), account.getTotalBalance(),
                account.getAvailableBalance(), account.getFrozenBalance(), account.getStatus());
    }

    /**
     * 通用账户加锁执行模板：先获取 Redis 分布式锁，确保同一账户的修改串行，再执行业务逻辑。
     */
    private <T> T executeWithLock(String accountId, AccountOperation<T> operation) {
        boolean locked = lockManager.tryLock(accountId, ACCOUNT_LOCK_TTL_SECONDS);
        if (!locked) {
            throw new BusinessException(ErrorCode.PROCESSING, "账户正在处理并发交易，请稍后重试");
        }
        try {
            Account account = findAccount(accountId);
            return operation.apply(account);
        } finally {
            lockManager.unlock(accountId);
        }
    }

    /**
     * 账户操作模板接口，便于在加锁后传入不同的业务逻辑。
     */
    private interface AccountOperation<T> {
        T apply(Account account);
    }
}
