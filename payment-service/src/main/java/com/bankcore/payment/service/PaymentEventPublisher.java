package com.bankcore.payment.service;

import com.bankcore.payment.service.messaging.PaymentEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate,
                                 @Value("${payment.messaging.exchange:payment.events.exchange}") String exchange,
                                 @Value("${payment.messaging.routingKey:payment.events}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishAsync(String requestId, String instructionId) {
        PaymentEvent event = new PaymentEvent();
        event.setRequestId(requestId);
        event.setInstructionId(instructionId);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
