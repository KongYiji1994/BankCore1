package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationChannel;
import com.bankcore.notification.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookChannelHandler implements NotificationChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(WebhookChannelHandler.class);
    private final WebClient webClient;

    public WebhookChannelHandler(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return NotificationChannel.WEBHOOK == channel;
    }

    @Override
    public void send(NotificationMessage message) {
        try {
            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("referenceId", message.getReferenceId());
            payload.put("eventType", message.getEventType() == null ? "GENERIC" : message.getEventType().name());
            payload.put("subject", message.getSubject());
            payload.put("content", message.getContent());

            webClient.post()
                    .uri(message.getDestination())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Webhook notification posted to {} for {}", message.getDestination(), message.getReferenceId());
        } catch (Exception e) {
            log.warn("Webhook notification failed to {}: {}", message.getDestination(), e.getMessage());
        }
    }
}
