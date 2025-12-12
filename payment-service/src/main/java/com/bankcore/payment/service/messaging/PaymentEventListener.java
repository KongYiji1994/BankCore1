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
import com.bankcore.payment.service.PaymentRiskAssessor;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);
    private final PaymentRepository paymentRepository;
    private final PaymentRequestRepository requestRepository;
    private final PaymentRiskAssessor riskAssessor;
    private final PaymentClearingAdapter clearingAdapter;
    private final AccountClient accountClient;
    private final RiskClient riskClient;

    public PaymentEventListener(PaymentRepository paymentRepository,
                                PaymentRequestRepository requestRepository,
                                PaymentRiskAssessor riskAssessor,
                                PaymentClearingAdapter clearingAdapter,
                                AccountClient accountClient,
                                RiskClient riskClient) {
        this.paymentRepository = paymentRepository;
        this.requestRepository = requestRepository;
        this.riskAssessor = riskAssessor;
        this.clearingAdapter = clearingAdapter;
        this.accountClient = accountClient;
        this.riskClient = riskClient;
    }

    @RabbitListener(queues = "${payment.messaging.queue:payment.events.queue}")
    public void onPaymentEvent(PaymentEvent event) {
        if (event == null || event.getInstructionId() == null) {
            return;
        }
        log.info("received payment event requestId={}, instructionId={}", event.getRequestId(), event.getInstructionId());
        PaymentRequestRecord requestRecord = requestRepository.findByRequestId(event.getRequestId()).orElse(null);
        if (requestRecord != null && requestRecord.getStatus() == PaymentRequestStatus.SUCCEEDED) {
            log.info("Duplicate payment request {} ignored: already succeeded", event.getRequestId());
            return;
        }
        PaymentInstruction instruction = paymentRepository.findById(event.getInstructionId())
                .orElse(null);
        if (instruction == null) {
            requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.FAILED, event.getInstructionId(), "instruction missing");
            return;
        }
        requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.PROCESSING, instruction.getInstructionId(), null);
        paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.IN_RISK_REVIEW);
        try {
            RiskClient.RiskDecisionResponse decision = riskClient.evaluate(instruction.getAmount(), instruction.getPayerCustomerId(), instruction.getChannel() == null ? "API" : instruction.getChannel(), instruction.getPayerAccount());
            if (decision != null && decision.getResult() != null && "REJECTED".equalsIgnoreCase(decision.getResult())) {
                paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.RISK_REJECTED);
                requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.SUCCEEDED, instruction.getInstructionId(), decision.getReason());
                return;
            }
            if (decision != null && decision.getResult() != null && "REVIEW".equalsIgnoreCase(decision.getResult())) {
                paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.IN_RISK_REVIEW);
                requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.PROCESSING, instruction.getInstructionId(), decision.getReason());
                return;
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
                return;
            }
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
        } catch (Exception ex) {
            log.error("Payment processing failed for {}", event.getInstructionId(), ex);
            paymentRepository.updateStatus(instruction.getInstructionId(), PaymentStatus.FAILED);
            requestRepository.updateStatus(event.getRequestId(), PaymentRequestStatus.FAILED, instruction.getInstructionId(), ex.getMessage());
        }
    }
}
