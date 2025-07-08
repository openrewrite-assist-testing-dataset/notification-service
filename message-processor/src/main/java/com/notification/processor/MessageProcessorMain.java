package com.notification.processor;

import com.notification.processor.service.NotificationProcessor;
import com.notification.processor.scheduler.ProcessingScheduler;

public class MessageProcessorMain {
    public static void main(String[] args) {
        NotificationProcessor processor = new NotificationProcessor();
        ProcessingScheduler scheduler = new ProcessingScheduler(processor);
        
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
        
        scheduler.start();
    }
}