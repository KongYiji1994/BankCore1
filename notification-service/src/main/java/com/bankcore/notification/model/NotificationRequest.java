package com.bankcore.notification.model;

import javax.validation.constraints.NotBlank;

public class NotificationRequest {
    /** 通知渠道（如EMAIL、WEBHOOK） */
    @NotBlank
    private String channel;
    /** 目标地址 */
    @NotBlank
    private String destination;
    /** 消息内容 */
    @NotBlank
    private String content;
    /** 消息标题 */
    private String subject;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
