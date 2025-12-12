package com.bankcore.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class NotificationMessagingConfig {

    @Bean
    public DirectExchange notificationExchange(@Value("${notification.messaging.exchange:notification.events.exchange}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue notificationQueue(@Value("${notification.messaging.queue:notification.events.queue}") String queue,
                                   @Value("${notification.messaging.dlq:notification.events.dlq}") String dlq) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", dlq);
        return new Queue(queue, true, false, false, args);
    }

    @Bean
    public Queue notificationDlq(@Value("${notification.messaging.dlq:notification.events.dlq}") String dlq) {
        return new Queue(dlq, true);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange,
                                       @Value("${notification.messaging.routingKey:notification.events}") String routingKey) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
