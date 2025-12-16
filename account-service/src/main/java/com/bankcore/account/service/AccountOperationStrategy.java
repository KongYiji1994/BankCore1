package com.bankcore.account.service;

import com.bankcore.common.dto.AccountDTO;
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
     * @return 操作后的账户信息
     */
    AccountDTO execute(String accountId, BigDecimal amount, String requestId);
}
