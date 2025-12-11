package com.bankcore.notification.model;

import javax.validation.constraints.NotBlank;

public class NotificationRequest {
    @NotBlank
    private String channel;
    @NotBlank
    private String destination;
    @NotBlank
    private String content;

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
}
