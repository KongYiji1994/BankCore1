package com.bankcore.payment.api;

import com.bankcore.common.dto.PaymentBatchResult;
import com.bankcore.common.dto.PaymentRequest;
import com.bankcore.payment.model.PaymentInstruction;
import com.bankcore.payment.service.PaymentService;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付接口：受理支付请求、批量处理以及状态操作入口。
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 构造注入支付服务。
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 提交支付请求，返回入队后的指令。
     */
    @PostMapping
    public ResponseEntity<PaymentInstruction> submit(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.submit(request));
    }

    /**
     * 手动触发单条指令的异步处理。
     */
    @PostMapping("/{instructionId}/process")
    public ResponseEntity<PaymentInstruction> process(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.enqueueForProcessing(instructionId));
    }

    /**
     * 批量触发处理。
     */
    @PostMapping("/batch/process")
    public ResponseEntity<PaymentBatchResult> processBatch(@RequestBody List<String> instructionIds) {
        return ResponseEntity.ok(paymentService.processBatch(instructionIds));
    }

    /**
     * 风控人工审核通过。
     */
    @PostMapping("/{instructionId}/risk-approve")
    public ResponseEntity<PaymentInstruction> approve(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.riskApprove(instructionId));
    }

    /**
     * 清算成功后标记为已入账。
     */
    @PostMapping("/{instructionId}/post")
    public ResponseEntity<PaymentInstruction> post(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.post(instructionId));
    }

    /**
     * 失败补偿或人工标记失败。
     */
    @PostMapping("/{instructionId}/fail")
    public ResponseEntity<PaymentInstruction> fail(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.fail(instructionId));
    }

    /**
     * 查询全部指令。
     */
    @GetMapping
    public ResponseEntity<List<PaymentInstruction>> list() {
        return ResponseEntity.ok(paymentService.list());
    }

    /**
     * 查询单条指令。
     */
    @GetMapping("/{instructionId}")
    public ResponseEntity<PaymentInstruction> get(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentService.get(instructionId));
    }
}
