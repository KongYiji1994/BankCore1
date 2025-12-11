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

@RestController
@RequestMapping("/accounts")
@Validated
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AccountDTO> create(@RequestParam @NotBlank String customerId,
                                             @RequestParam @NotBlank String currency,
                                             @RequestParam(defaultValue = "0") @Min(0) BigDecimal openingBalance) {
        return ResponseEntity.ok(service.createAccount(customerId, currency, openingBalance));
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> list(@RequestParam(value = "customerId", required = false) String customerId) {
        if (customerId != null && customerId.trim().length() > 0) {
            return ResponseEntity.ok(service.listByCustomer(customerId));
        }
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> get(@PathVariable String accountId) {
        return ResponseEntity.ok(service.get(accountId));
    }

    @PostMapping("/{accountId}/credit")
    public ResponseEntity<AccountDTO> credit(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.credit(accountId, amount));
    }

    @PostMapping("/{accountId}/debit")
    public ResponseEntity<AccountDTO> debit(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.freezeAmount(accountId, amount));
    }

    @PostMapping("/{accountId}/freeze")
    public ResponseEntity<AccountDTO> freeze(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.freezeAmount(accountId, amount));
    }

    @PostMapping("/{accountId}/settle")
    public ResponseEntity<AccountDTO> settle(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.settle(accountId, amount));
    }

    @PostMapping("/{accountId}/unfreeze")
    public ResponseEntity<AccountDTO> unfreeze(@PathVariable String accountId, @RequestParam @Min(1) BigDecimal amount) {
        return ResponseEntity.ok(service.unfreeze(accountId, amount));
    }

    @PostMapping("/{accountId}/close")
    public ResponseEntity<AccountDTO> close(@PathVariable String accountId) {
        return ResponseEntity.ok(service.close(accountId));
    }
}
