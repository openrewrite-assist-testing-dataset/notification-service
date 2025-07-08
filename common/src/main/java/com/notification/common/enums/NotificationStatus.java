package com.notification.common.enums;

public enum NotificationStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    SENT("sent"),
    FAILED("failed"),
    CANCELLED("cancelled"),
    SCHEDULED("scheduled");
    
    private final String value;
    
    NotificationStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static NotificationStatus fromValue(String value) {
        for (NotificationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown notification status: " + value);
    }
    
    public boolean isTerminal() {
        return this == SENT || this == FAILED || this == CANCELLED;
    }
}