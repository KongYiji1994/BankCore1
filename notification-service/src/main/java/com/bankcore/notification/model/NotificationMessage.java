package com.bankcore.notification.model;

public class NotificationMessage {
    /** 通知渠道 */
    private NotificationChannel channel;
    /** 发送目标地址 */
    private String destination;
    /** 通知标题 */
    private String subject;
    /** 通知正文内容 */
    private String content;
    /** 关联事件类型 */
    private NotificationEventType eventType;
    /** 业务参考ID */
    private String referenceId;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

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
}
