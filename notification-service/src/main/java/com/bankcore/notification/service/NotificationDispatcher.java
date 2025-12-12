package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationChannel;
import com.bankcore.notification.model.NotificationMessage;
import com.bankcore.notification.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationDispatcher {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private final List<NotificationChannelHandler> handlers;

    public NotificationDispatcher(List<NotificationChannelHandler> handlers) {
        this.handlers = handlers;
    }

    @Async("notificationExecutor")
    public void dispatch(NotificationRequest request) {
        NotificationMessage message = new NotificationMessage();
        message.setChannel(parseChannel(request.getChannel()));
        message.setDestination(request.getDestination());
        message.setContent(request.getContent());
        message.setSubject(request.getSubject());
        message.setReferenceId("manual-api");
        doSend(message);
    }

    @Async("notificationExecutor")
    public void dispatch(NotificationMessage message) {
        doSend(message);
    }

    private void doSend(NotificationMessage message) {
        NotificationChannel channel = message.getChannel();
        if (channel == null) {
            log.warn("No channel provided for reference {}", message.getReferenceId());
            return;
        }
        for (NotificationChannelHandler handler : handlers) {
            if (handler.supports(channel)) {
                handler.send(message);
                return;
            }
        }
        log.warn("No handler configured for channel {}", channel);
    }

    private NotificationChannel parseChannel(String rawChannel) {
        if (rawChannel == null) {
            return null;
        }
        try {
            return NotificationChannel.valueOf(rawChannel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Unsupported channel {}", rawChannel);
            return null;
        }
    }
}
