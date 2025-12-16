package com.bankcore.notification.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

public class NotificationEvent implements Serializable {
    /** 事件类型 */
    @NotNull
    private NotificationEventType eventType;
    /** 业务参考ID */
    @NotBlank
    private String referenceId;
    /** 通知渠道 */
    private NotificationChannel channel;
    /** 发送目标（邮箱、Webhook地址等） */
    private String destination;
    /** 通知标题 */
    private String subject;
    /** 通知内容 */
    private String message;
    /** 额外元数据 */
    private Map<String, Object> metadata;

    public NotificationEventType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationEventType eventType) {
        this.eventType = eventType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
