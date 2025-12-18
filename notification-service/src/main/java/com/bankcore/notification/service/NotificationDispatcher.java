package com.bankcore.notification.service;

import com.bankcore.notification.model.NotificationChannel;
import com.bankcore.notification.model.NotificationMessage;
import com.bankcore.notification.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 通知分发器：根据消息通道路由到具体的发送处理器，异步执行避免阻塞业务线程。
 */
@Service
public class NotificationDispatcher {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private final Map<NotificationChannel, NotificationChannelHandler> handlers;

    /**
     * 构造注入所有通道处理器。
     */
    public NotificationDispatcher(List<NotificationChannelHandler> handlers) {
        this.handlers = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelHandler handler : handlers) {
            NotificationChannel channel = handler.getChannel();
            if (channel == null) {
                log.warn("Handler {} returned null channel and will be ignored", handler.getClass().getSimpleName());
                continue;
            }
            NotificationChannelHandler existing = this.handlers.put(channel, handler);
            if (existing != null) {
                log.warn("Duplicate handler for channel {} detected: {} replaced {}", channel, handler.getClass().getSimpleName(), existing.getClass().getSimpleName());
            }
        }
    }

    /**
     * REST 提交的通知请求异步发送。
     */
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

    /**
     * MQ 转换后的通知消息异步发送。
     */
    @Async("notificationExecutor")
    public void dispatch(NotificationMessage message) {
        doSend(message);
    }

    /**
     * 通道选择与发送主流程，未匹配到处理器时记录告警。
     */
    private void doSend(NotificationMessage message) {
        NotificationChannel channel = message.getChannel();
        if (channel == null) {
            log.warn("No channel provided for reference {}", message.getReferenceId());
            return;
        }
        NotificationChannelHandler handler = handlers.get(channel);
        if (handler != null) {
            handler.send(message);
            return;
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
