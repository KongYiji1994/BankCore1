package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationChannel;
import com.bankcore.notification.model.NotificationMessage;

public interface NotificationChannelHandler {
    NotificationChannel getChannel();

    void send(NotificationMessage message);
}
