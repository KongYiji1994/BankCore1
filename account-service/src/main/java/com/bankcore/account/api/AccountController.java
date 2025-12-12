package com.bankcore.account.api;

import com.bankcore.account.service.AccountService;
import com.bankcore.common.dto.AccountDTO;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户接口，提供开户、查询以及三余额相关的增减、冻结、解冻和关闭操作。
 */
@RestController
@RequestMapping("/accounts")
@Validated
public class AccountController {

    private final AccountService service;

    /**
     * 构造注入账户服务。
     */
    public AccountController(AccountService service) {
        this.service = service;
    }

    /**
     * 创建账户，支持指定客户、币种及初始余额。
     */
    @PostMapping
    public ResponseEntity<AccountDTO> create(@RequestParam @NotBlank String customerId,
                                             @RequestParam @NotBlank String currency,
                                             @RequestParam(defaultValue = "0") @Min(0) BigDecimal openingBalance) {
        return ResponseEntity.ok(service.createAccount(customerId, currency, openingBalance));
    }

    /**
     * 查询账户列表，可按客户维度过滤。
     */
    @GetMapping
    public ResponseEntity<List<AccountDTO>> list(@RequestParam(value = "customerId", required = false) String customerId) {
        if (customerId != null && customerId.trim().length() > 0) {
            return ResponseEntity.ok(service.listByCustomer(customerId));
        }
        return ResponseEntity.ok(service.list());
    }

    /**
     * 通过账户号查询账户详情。
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> get(@PathVariable String accountId) {
        return ResponseEntity.ok(service.get(accountId));
    }

    /**
     * 收款入账。
     */
    @PostMapping("/{accountId}/credit")
    public ResponseEntity<AccountDTO> credit(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.credit(accountId, amount));
    }

    /**
     * 支付前冻结（兼容旧接口）。
     */
    @PostMapping("/{accountId}/debit")
    public ResponseEntity<AccountDTO> debit(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.freezeAmount(accountId, amount));
    }

    /**
     * 冻结指定金额。
     */
    @PostMapping("/{accountId}/freeze")
    public ResponseEntity<AccountDTO> freeze(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.freezeAmount(accountId, amount));
    }

    /**
     * 清算成功，扣减冻结与总余额。
     */
    @PostMapping("/{accountId}/settle")
    public ResponseEntity<AccountDTO> settle(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.settle(accountId, amount));
    }

    /**
     * 取消支付或失败时解除冻结。
     */
    @PostMapping("/{accountId}/unfreeze")
    public ResponseEntity<AccountDTO> unfreeze(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.unfreeze(accountId, amount));
    }

    /**
     * 关闭账户：需三余额均为 0。
     */
    @PostMapping("/{accountId}/close")
    public ResponseEntity<AccountDTO> close(@PathVariable String accountId) {
        return ResponseEntity.ok(service.close(accountId));
    }
}
