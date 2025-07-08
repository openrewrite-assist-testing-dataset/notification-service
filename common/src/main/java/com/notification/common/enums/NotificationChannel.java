package com.notification.common.enums;

public enum NotificationChannel {
    EMAIL("email"),
    SMS("sms"),
    PUSH("push"),
    SLACK("slack"),
    WEBHOOK("webhook");
    
    private final String value;
    
    NotificationChannel(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static NotificationChannel fromValue(String value) {
        for (NotificationChannel channel : values()) {
            if (channel.value.equals(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown notification channel: " + value);
    }
}