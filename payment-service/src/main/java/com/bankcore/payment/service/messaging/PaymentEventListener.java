package com.bankcore.payment.service.messaging;

import com.bankcore.common.dto.PaymentStatus;
import com.bankcore.payment.client.AccountClient;
import com.bankcore.payment.client.RiskClient;
import com.bankcore.payment.model.PaymentInstruction;
import com.bankcore.payment.model.PaymentRequestRecord;
import com.bankcore.payment.model.PaymentRequestStatus;
import com.bankcore.payment.repository.PaymentRepository;
import com.bankcore.payment.repository.PaymentRequestRepository;
import com.bankcore.payment.service.PaymentClearingAdapter;
import com.bankcore.payment.service.PaymentIdempotencyManager;
import com.bankcore.payment.service.PaymentAccountLockManager;
import com.bankcore.payment.service.PaymentRiskAssessor;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付事件消费者：监听 MQ 消息，执行风控->冻结->清算的异步链路，并用 Redis 标记避免重复消费。
 */
@Component
public class PaymentEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);
    private static final long EVENT_LOCK_TTL_SECONDS = 300L;
    private static final long EVENT_DONE_TTL_SECONDS = 3600L;
    private static final long ACCOUNT_LOCK_TTL_SECONDS = 60L;
    private final PaymentRepository paymentRepository;
    private final PaymentRequestRepository requestRepository;
    private final PaymentRiskAssessor riskAssessor;
    private final PaymentClearingAdapter clearingAdapter;
    private final AccountClient accountClient;
    private final RiskClient riskClient;
    private final PaymentIdempotencyManager idempotencyManager;
    private final PaymentAccountLockManager accountLockManager;

    /**
     * 构造函数注入依赖，方便单测与替换实现。
     */
    public PaymentEventListener(PaymentRepository paymentRepository,
                                PaymentRequestRepository requestRepository,
                                PaymentRiskAssessor riskAssessor,
                                PaymentClearingAdapter clearingAdapter,
                                AccountClient accountClient,
                                RiskClient riskClient,
                                PaymentIdempotencyManager idempotencyManager,
                                PaymentAccountLockManager accountLockManager) {
        this.paymentRepository = paymentRepository;
        this.requestRepository = requestRepository;
        this.riskAssessor = riskAssessor;
        this.clearingAdapter = clearingAdapter;
        this.accountClient = accountClient;
        this.riskClient = riskClient;
        this.idempotencyManager = idempotencyManager;
        this.accountLockManager = accountLockManager;
    }

    /**
     * MQ 监听入口：幂等检查 -> 风控 -> 冻结记账 -> 清算 -> 更新请求/指令状态。
     */
    @RabbitListener(queues = "${payment.messaging.queue:payment.events.queue}")
    public void onPaymentEvent(PaymentEvent event) {
        if (event == null || event.getInstructionId() == null) {
            return;
        }
        if (idempotencyManager.isEventAlreadyProcessed(event.getInstructionId())) {
            log.info("payment event {} already processed, skipping", event.getInstructionId());
            return;
        }
        boolean locked = idempotencyManager.tryAcquireEventProcessing(event.getInstructionId(), EVENT_LOCK_TTL_SECONDS);
        if (!locked) {
            log.info("payment event {} is already being processed", event.getInstructionId());
            return;
        }
        log.info("received payment event requestId={}, instructionId={}", event.getRequestId(), event.getInstructionId());
        try {
            PaymentRequestRecord requestRecord = requestRepository.findByRequestId(event.getRequestId()).orElse(null);
            if (requestRecord != null && requestRecord.getStatus() == PaymentRequestStatus.SUCCEEDED) {
                log.info("Duplicate payment request {} ignored: already succeeded", event.getRequestId());
                idempotencyManager.markEventCompleted(event.getInstructionId(), EVENT_DONE_TTL_SECONDS);
                return;
            }
            PaymentInstruction instruction = paymentRepository.findById(event.getInstructionId())
                    .orElse(null);
            if (instruction == null) {
                requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.FAILED, event.getInstructionId(), "instruction missing");
                idempotencyManager.markEventCompleted(event.getInstructionId(), EVENT_DONE_TTL_SECONDS);
                return;
            }
            requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.PROCESSING, instruction.getInstructionId(), null);
            paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.IN_RISK_REVIEW);
            RiskClient.RiskDecisionResponse decision = riskClient.evaluate(instruction.getAmount(), instruction.getPayerCustomerId(), instruction.getChannel() == null ? "API" : instruction.getChannel(), instruction.getPayerAccount());
            if (decision != null && decision.getResult() != null && "REJECTED".equalsIgnoreCase(decision.getResult())) {
                paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.RISK_REJECTED);
                requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.SUCCEEDED, instruction.getInstructionId(), decision.getReason());
                idempotencyManager.markEventCompleted(event.getInstructionId(), EVENT_DONE_TTL_SECONDS);
                return;
            }
            if (decision != null && decision.getResult() != null && "REVIEW".equalsIgnoreCase(decision.getResult())) {
                paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.IN_RISK_REVIEW);
                requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.PROCESSING, instruction.getInstructionId(), decision.getReason());
                idempotencyManager.markEventCompleted(event.getInstructionId(), EVENT_DONE_TTL_SECONDS);
                return;
            }
            // 在真正冻结/扣款前，使用账户维度分布式锁保证同一账户不会被并发出款
            boolean accountLocked = accountLockManager.tryLock(instruction.getPayerAccount(), ACCOUNT_LOCK_TTL_SECONDS);
            if (!accountLocked) {
                log.warn("payer account {} is locked by concurrent payment, will retry", instruction.getPayerAccount());
                throw new IllegalStateException("账户正在处理其他支付，稍后重试");
            }
            BigDecimal score = riskAssessor.evaluate(instruction);
            PaymentStatus nextStatus = score.compareTo(BigDecimal.valueOf(80)) >= 0
                    ? PaymentStatus.RISK_REJECTED
                    : PaymentStatus.RISK_APPROVED;
            paymentRepository.updateRisk(instruction.getInstructionId(), score, nextStatus);
            instruction.setRiskScore(score);
            instruction.setStatus(nextStatus);
            if (nextStatus == PaymentStatus.RISK_REJECTED) {
                requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.SUCCEEDED, instruction.getInstructionId(), "rejected by risk");
                idempotencyManager.markEventCompleted(event.getInstructionId(), EVENT_DONE_TTL_SECONDS);
                accountLockManager.unlock(instruction.getPayerAccount());
                return;
            }
            try {
                accountClient.freeze(instruction.getPayerAccount(), instruction.getAmount());
                PaymentStatus clearingStatus = clearingAdapter.dispatch(instruction);
                if (clearingStatus == PaymentStatus.POSTED) {
                    accountClient.settle(instruction.getPayerAccount(), instruction.getAmount());
                    paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.POSTED);
                    requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.SUCCEEDED, instruction.getInstructionId(), "posted");
                } else if (clearingStatus == PaymentStatus.CLEARING) {
                    paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.CLEARING);
                    requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.SUCCEEDED, instruction.getInstructionId(), "sent to clearing");
                } else {
                    paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.FAILED);
                    requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.FAILED, instruction.getInstructionId(), "clearing failed");
                    accountClient.unfreeze(instruction.getPayerAccount(), instruction.getAmount());
                }
                idempotencyManager.markEventCompleted(event.getInstructionId(), EVENT_DONE_TTL_SECONDS);
            } finally {
                accountLockManager.unlock(instruction.getPayerAccount());
            }
        } catch (Exception ex) {
            log.error("Payment processing failed for {}", event.getInstructionId(), ex);
            idempotencyManager.releaseEventLock(event.getInstructionId());
            throw ex;
        }
    }
}
