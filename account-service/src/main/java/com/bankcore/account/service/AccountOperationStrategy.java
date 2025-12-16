package com.bankcore.account.service;

import com.bankcore.account.model.Account;
import java.math.BigDecimal;

/**
 * 账户操作策略接口，便于在运行时根据操作类型选择不同的处理逻辑。
 */
public interface AccountOperationStrategy {

    /**
     * 返回策略对应的操作类型（如 CREDIT、FREEZE 等）。
     */
    AccountOperationType operationType();

    /**
     * 执行具体账户操作。
     *
     * @param accountId 账户ID
     * @param amount    操作金额
     * @param requestId 幂等请求ID
     */
    void applyOperation(Account account, BigDecimal amount);

    /**
     * 是否需要基于请求ID做幂等处理。
     */
    default boolean requiresIdempotency() {
        return true;
    }

    /**
     * 是否需要生成分录。
     */
    default boolean shouldRecordLedger() {
        return true;
    }

    /**
     * 操作提交后的后置处理。
     */
    default void afterCommit(String accountId, BigDecimal amount, String requestId) {
        // 默认无附加动作
    }
}
