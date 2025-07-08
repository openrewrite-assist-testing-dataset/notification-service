package com.notification.processor.service;

import com.notification.common.model.NotificationMessage;
import com.notification.common.enums.NotificationChannel;
import com.notification.common.enums.NotificationStatus;
import com.notification.common.util.DateTimeUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NotificationProcessor {
    private final JedisPool jedisPool;
    private boolean running = false;

    public NotificationProcessor() {
        this.jedisPool = new JedisPool("localhost", 6379);
    }

    public void startProcessing() {
        running = true;
        
        // Process different channels
        processChannel(NotificationChannel.EMAIL);
        processChannel(NotificationChannel.SMS);
        processChannel(NotificationChannel.SLACK);
        processChannel(NotificationChannel.PUSH);
        processChannel(NotificationChannel.WEBHOOK);
    }

    private void processChannel(NotificationChannel channel) {
        new Thread(() -> {
            while (running) {
                try (Jedis jedis = jedisPool.getResource()) {
                    String queueKey = "notification_queue:" + channel.getValue();
                    String notificationId = jedis.rpop(queueKey);
                    
                    if (notificationId != null) {
                        processNotification(notificationId, channel);
                    } else {
                        // No notifications to process, wait a bit
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing " + channel + " notifications: " + e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }).start();
    }

    private void processNotification(String notificationId, NotificationChannel channel) {
        System.out.println("Processing notification " + notificationId + " for channel " + channel);
        
        // Simulate processing time
        try {
            Thread.sleep(500 + (long)(Math.random() * 1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate success/failure
        boolean success = Math.random() > 0.1; // 90% success rate
        
        if (success) {
            System.out.println("Successfully processed notification " + notificationId);
        } else {
            System.out.println("Failed to process notification " + notificationId);
        }
    }

    public void stopProcessing() {
        running = false;
        jedisPool.close();
    }
}