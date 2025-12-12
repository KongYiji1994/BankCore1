package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationChannel;
import com.bankcore.notification.model.NotificationMessage;

public interface NotificationChannelHandler {
    boolean supports(NotificationChannel channel);

    void send(NotificationMessage message);
}
