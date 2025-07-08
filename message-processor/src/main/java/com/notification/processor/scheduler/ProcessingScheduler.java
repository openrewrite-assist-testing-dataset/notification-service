package com.notification.processor.scheduler;

import com.notification.processor.service.NotificationProcessor;

public class ProcessingScheduler {
    private final NotificationProcessor processor;
    private boolean running = false;

    public ProcessingScheduler(NotificationProcessor processor) {
        this.processor = processor;
    }

    public void start() {
        System.out.println("Starting notification message processor...");
        running = true;
        
        processor.startProcessing();
        
        // Keep main thread alive
        try {
            while (running) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        System.out.println("Shutting down notification message processor...");
        running = false;
        processor.stopProcessing();
    }
}