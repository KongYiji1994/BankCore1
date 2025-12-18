package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationChannel;
import com.bankcore.notification.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailChannelHandler implements NotificationChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(EmailChannelHandler.class);
    private final JavaMailSender mailSender;

    public EmailChannelHandler(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationMessage message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(message.getDestination());
            mailMessage.setSubject(message.getSubject() == null ? "BankCore Notification" : message.getSubject());
            mailMessage.setText(message.getContent());
            mailSender.send(mailMessage);
            log.info("Email notification sent to {} for {}", message.getDestination(), message.getReferenceId());
        } catch (Exception e) {
            log.warn("Email notification failed to {}: {}", message.getDestination(), e.getMessage());
        }
    }
}
