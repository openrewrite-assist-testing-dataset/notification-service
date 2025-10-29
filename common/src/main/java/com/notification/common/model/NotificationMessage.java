package com.notification.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.notification.common.enums.NotificationChannel;
import com.notification.common.enums.NotificationPriority;
import com.notification.common.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class NotificationMessage {
    
    @JsonProperty
    private String id;
    
    @JsonProperty
    @NotNull
    @Size(min = 1, max = 255)
    private String title;
    
    @JsonProperty
    @NotNull
    @Size(min = 1, max = 5000)
    private String message;
    
    @JsonProperty
    @NotNull
    private NotificationChannel channel;
    
    @JsonProperty
    @NotNull
    private NotificationPriority priority;
    
    @JsonProperty
    private NotificationStatus status;
    
    @JsonProperty
    @NotNull
    private String recipientId;
    
    @JsonProperty
    @Email
    private String recipientEmail;
    
    @JsonProperty
    private String recipientPhone;
    
    @JsonProperty
    private String recipientSlackChannel;
    
    @JsonProperty
    private Map<String, String> templateData;
    
    @JsonProperty
    private List<String> attachments;
    
    @JsonProperty
    private Date scheduledTime;
    
    @JsonProperty
    private Date createdAt;
    
    @JsonProperty
    private Date updatedAt;
    
    @JsonProperty
    private Date sentAt;
    
    @JsonProperty
    private int retryCount;
    
    @JsonProperty
    private String errorMessage;
    
    @JsonProperty
    private String templateId;
    
    @JsonProperty
    private String batchId;

    public NotificationMessage() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = NotificationStatus.PENDING;
        this.priority = NotificationPriority.MEDIUM;
        this.templateData = new HashMap<>();
        this.attachments = new ArrayList<>();
        this.retryCount = 0;
    }

    public NotificationMessage(String title, String message, NotificationChannel channel, String recipientId) {
        this();
        this.title = title;
        this.message = message;
        this.channel = channel;
        this.recipientId = recipientId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    
    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }
    
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    
    public String getRecipientSlackChannel() { return recipientSlackChannel; }
    public void setRecipientSlackChannel(String recipientSlackChannel) { this.recipientSlackChannel = recipientSlackChannel; }
    
    public Map<String, String> getTemplateData() { return templateData; }
    public void setTemplateData(Map<String, String> templateData) { this.templateData = templateData; }
    
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    
    public Date getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Date scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    
    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = new Date();
    }
    
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = new Date();
        this.updatedAt = new Date();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = new Date();
    }
}