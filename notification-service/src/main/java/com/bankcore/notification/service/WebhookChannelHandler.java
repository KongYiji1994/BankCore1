package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationChannel;
import com.bankcore.notification.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookChannelHandler implements NotificationChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(WebhookChannelHandler.class);
    private final RestTemplate restTemplate = new RestTemplate();

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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(message.getDestination(), new HttpEntity<Map<String, Object>>(payload, headers), Void.class);
            log.info("Webhook notification posted to {} for {}", message.getDestination(), message.getReferenceId());
        } catch (Exception e) {
            log.warn("Webhook notification failed to {}: {}", message.getDestination(), e.getMessage());
        }
    }
}
