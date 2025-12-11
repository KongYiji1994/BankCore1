package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatcher {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    public void dispatch(NotificationRequest request) {
        log.info("Dispatching {} to {} with payload: {}", request.getChannel(), request.getDestination(), request.getContent());
    }
}
