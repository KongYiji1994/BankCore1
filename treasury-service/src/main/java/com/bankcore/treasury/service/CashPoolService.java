package com.bankcore.treasury.service;

import com.bankcore.common.dto.AccountDTO;
import com.bankcore.common.dto.CashPoolDefinition;
import com.bankcore.treasury.client.AccountClient;
import com.bankcore.treasury.model.CashPool;
import com.bankcore.treasury.model.CashPoolInterestEntry;
import com.bankcore.treasury.repository.CashPoolInterestRepository;
import com.bankcore.treasury.repository.CashPoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 资金池服务：支持资金池注册、每日利息计提、物理池自动归集与补偿。
 */
@Service
public class CashPoolService {
    private static final BigDecimal DEFAULT_DAILY_RATE = new BigDecimal("0.0003");
    private static final String POOL_TYPE_PHYSICAL = "PHYSICAL";
    private final CashPoolRepository repository;
    private final CashPoolInterestRepository interestRepository;
    private final AccountClient accountClient;
    private final Logger log = LoggerFactory.getLogger(CashPoolService.class);

    /**
     * 构造注入仓储与账户客户端。
     */
    public CashPoolService(CashPoolRepository repository,
                           CashPoolInterestRepository interestRepository,
                           AccountClient accountClient) {
        this.repository = repository;
        this.interestRepository = interestRepository;
        this.accountClient = accountClient;
    }

    /**
     * 注册资金池定义，保存策略、类型与利率。
     */
    @Transactional
    public CashPool register(CashPoolDefinition definition) {
        CashPool pool = new CashPool(definition.getPoolId(), definition.getHeaderAccount(), definition.getMemberAccounts(), definition.getTargetBalance(), definition.getStrategy(),
                definition.getPoolType(), definition.getInterestRate());
        repository.save(pool);
        return pool;
    }

    /**
     * 日终利息计提：读取头寸余额，按利率计算并生成利息凭证，更新最后计提日期。
     */
    @Transactional
    public CashPoolInterestEntry accrueDailyInterest(String poolId) {
        CashPool pool = get(poolId);
        LocalDate today = LocalDate.now();
        if (today.equals(pool.getLastInterestDate())) {
            log.info("Interest already posted today for pool {}", poolId);
            List<CashPoolInterestEntry> entries = interestRepository.findByPoolAndDate(poolId, today);
            return entries.isEmpty() ? null : entries.get(0);
        }

        AccountDTO headerAccount = accountClient.getAccount(pool.getHeaderAccount());
        BigDecimal principal = headerAccount != null && headerAccount.getTotalBalance() != null
                ? headerAccount.getTotalBalance()
                : BigDecimal.ZERO;
        BigDecimal rate = pool.getInterestRate() != null ? pool.getInterestRate() : DEFAULT_DAILY_RATE;
        BigDecimal interestAmount = principal.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        CashPoolInterestEntry entry = new CashPoolInterestEntry();
        entry.setPoolId(pool.getPoolId());
        entry.setHeaderAccount(pool.getHeaderAccount());
        entry.setInterestAmount(interestAmount);
        entry.setRate(rate);
        entry.setAccrualDate(today);
        entry.setDescription("Daily interest accrual for header balance " + principal);
        interestRepository.save(entry);
        repository.updateInterestDate(poolId, today);
        log.info("Accrued interest {} for pool {} on {}, principal {} rate {}", interestAmount, poolId, today, principal, rate);
        return entry;
    }

    /**
     * 物理归集：在成员账户与头寸账户之间做目标余额调节。
     */
    @Transactional
    public CashPool sweep(String poolId) {
        CashPool pool = get(poolId);
        if (!POOL_TYPE_PHYSICAL.equalsIgnoreCase(pool.getPoolType())) {
            log.info("Pool {} is {} type, skipping physical sweep", pool.getPoolId(), pool.getPoolType());
            return pool;
        }
        AccountDTO headerAccount = accountClient.getAccount(pool.getHeaderAccount());
        Set<String> members = pool.getMemberAccounts();
        for (String memberAccount : members) {
            try {
                rebalanceMember(pool, headerAccount, memberAccount);
                headerAccount = accountClient.getAccount(pool.getHeaderAccount());
            } catch (Exception ex) {
                log.error("Failed to sweep member {} for pool {}", memberAccount, poolId, ex);
            }
        }
        return pool;
    }

    /**
     * 对全部资金池执行归集，供定时任务调用。
     */
    public void sweepAll() {
        List<CashPool> pools = repository.findAll();
        for (CashPool pool : pools) {
            sweep(pool.getPoolId());
        }
    }

    /**
     * 成员账户与头寸账户的调节逻辑：余额超目标则上收，不足则下拨。
     */
    private void rebalanceMember(CashPool pool, AccountDTO headerAccount, String memberAccountId) {
        AccountDTO member = accountClient.getAccount(memberAccountId);
        if (member == null || member.getAvailableBalance() == null) {
            log.warn("Cannot sweep member {} for pool {}, missing account info", memberAccountId, pool.getPoolId());
            return;
        }
        BigDecimal target = pool.getTargetBalance();
        BigDecimal memberAvailable = member.getAvailableBalance();
        if (memberAvailable.compareTo(target) > 0) {
            BigDecimal excess = memberAvailable.subtract(target);
            accountClient.debit(memberAccountId, excess);
            accountClient.settle(memberAccountId, excess);
            accountClient.credit(pool.getHeaderAccount(), excess);
            log.info("Swept {} from member {} to header {} for pool {}", excess, memberAccountId, pool.getHeaderAccount(), pool.getPoolId());
        } else if (memberAvailable.compareTo(target) < 0 && headerAccount != null && headerAccount.getAvailableBalance() != null) {
            BigDecimal needed = target.subtract(memberAvailable);
            if (headerAccount.getAvailableBalance().compareTo(needed) > 0) {
                accountClient.debit(pool.getHeaderAccount(), needed);
                accountClient.settle(pool.getHeaderAccount(), needed);
                accountClient.credit(memberAccountId, needed);
                log.info("Funded member {} with {} from header {} to reach target {}", memberAccountId, needed, pool.getHeaderAccount(), target);
            } else {
                log.warn("Header account {} lacks available balance to fund member {} for pool {}", pool.getHeaderAccount(), memberAccountId, pool.getPoolId());
            }
        }
    }

    /**
     * 查询单个资金池。
     */
    public CashPool get(String poolId) {
        return repository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));
    }

    /**
     * 查询全部资金池列表。
     */
    public List<CashPool> list() {
        return repository.findAll();
    }
}
