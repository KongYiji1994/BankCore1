package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationChannel;
import com.bankcore.notification.model.NotificationEvent;
import com.bankcore.notification.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    private final NotificationDispatcher dispatcher;

    public NotificationEventListener(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @RabbitListener(queues = "${notification.messaging.queue:notification.events.queue}")
    public void onNotificationEvent(NotificationEvent event) {
        if (event == null) {
            return;
        }
        NotificationMessage message = new NotificationMessage();
        message.setReferenceId(event.getReferenceId());
        message.setEventType(event.getEventType());
        message.setContent(resolveContent(event));
        message.setSubject(event.getSubject());
        if (event.getChannel() != null) {
            message.setChannel(event.getChannel());
        } else {
            message.setChannel(NotificationChannel.EMAIL);
        }
        message.setDestination(event.getDestination());
        log.info("Received notification event {} for {}", event.getEventType(), event.getReferenceId());
        dispatcher.dispatch(message);
    }

    private String resolveContent(NotificationEvent event) {
        if (event.getMessage() != null) {
            return event.getMessage();
        }
        if (event.getEventType() == null) {
            return "BankCore notification";
        }
        switch (event.getEventType()) {
            case PAYMENT_SUCCESS:
                return "Payment " + event.getReferenceId() + " posted successfully.";
            case PAYMENT_FAILED:
                return "Payment " + event.getReferenceId() + " failed during clearing. Please review.";
            case RECONCILIATION_BREAK:
                return "Reconciliation break detected for reference " + event.getReferenceId() + ".";
            default:
                return "BankCore notification for " + event.getReferenceId();
        }
    }
}
