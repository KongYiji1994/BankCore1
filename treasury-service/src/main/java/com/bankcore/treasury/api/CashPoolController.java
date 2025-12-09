package com.bankcore.treasury.api;

import com.bankcore.common.dto.CashPoolDefinition;
import com.bankcore.treasury.model.CashPool;
import com.bankcore.treasury.service.CashPoolService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/pools")
public class CashPoolController {

    private final CashPoolService service;

    public CashPoolController(CashPoolService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CashPool> register(@Valid @RequestBody CashPoolDefinition definition) {
        return ResponseEntity.ok(service.register(definition));
    }

    @PostMapping("/{poolId}/sweep")
    public ResponseEntity<CashPool> sweep(@PathVariable String poolId, @RequestParam BigDecimal headerBalance) {
        return ResponseEntity.ok(service.sweep(poolId, headerBalance));
    }

    @GetMapping
    public ResponseEntity<List<CashPool>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{poolId}")
    public ResponseEntity<CashPool> get(@PathVariable String poolId) {
        return ResponseEntity.ok(service.get(poolId));
    }
}
