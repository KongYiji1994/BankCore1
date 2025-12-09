package com.bankcore.payment.api;

import com.bankcore.common.dto.PaymentBatchResult;
import com.bankcore.common.dto.PaymentRequest;
import com.bankcore.payment.model.PaymentInstruction;
import com.bankcore.payment.service.PaymentService;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentInstruction> submit(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.submit(request));
    }

    @PostMapping("/{instructionId}/process")
    public ResponseEntity<PaymentInstruction> process(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.processAsync(instructionId).join());
    }

    @PostMapping("/batch/process")
    public ResponseEntity<PaymentBatchResult> processBatch(@RequestBody List<String> instructionIds) {
        return ResponseEntity.ok(paymentService.processBatch(instructionIds));
    }

    @PostMapping("/{instructionId}/risk-approve")
    public ResponseEntity<PaymentInstruction> approve(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.riskApprove(instructionId));
    }

    @PostMapping("/{instructionId}/post")
    public ResponseEntity<PaymentInstruction> post(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.post(instructionId));
    }

    @PostMapping("/{instructionId}/fail")
    public ResponseEntity<PaymentInstruction> fail(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.fail(instructionId));
    }

    @GetMapping
    public ResponseEntity<List<PaymentInstruction>> list() {
        return ResponseEntity.ok(paymentService.list());
    }

    @GetMapping("/{instructionId}")
    public ResponseEntity<PaymentInstruction> get(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.get(instructionId));
    }
}
