package com.bankcore.payment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentMessagingConfig {

    @Bean
    public DirectExchange paymentExchange(@Value("${payment.messaging.exchange:payment.events.exchange}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue paymentQueue(@Value("${payment.messaging.queue:payment.events.queue}") String queue,
                              @Value("${payment.messaging.dlq:payment.events.dlq}") String dlq) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", dlq);
        return new Queue(queue, true, false, false, args);
    }

    @Bean
    public Queue paymentDlq(@Value("${payment.messaging.dlq:payment.events.dlq}") String dlq) {
        return new Queue(dlq, true);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, DirectExchange paymentExchange,
                                  @Value("${payment.messaging.routingKey:payment.events}") String routingKey) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with(routingKey);
    }
}
