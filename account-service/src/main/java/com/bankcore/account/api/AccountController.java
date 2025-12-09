package com.bankcore.account.api;

import com.bankcore.account.service.AccountService;
import com.bankcore.common.dto.AccountDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<List<AccountDTO>> list() {
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
        return ResponseEntity.ok(service.debit(accountId, amount));
    }
}
