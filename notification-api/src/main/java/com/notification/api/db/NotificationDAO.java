package com.notification.api.db;

import com.notification.common.model.NotificationMessage;
import com.notification.common.enums.NotificationStatus;
import com.notification.common.enums.NotificationChannel;
import com.notification.common.enums.NotificationPriority;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RegisterBeanMapper(NotificationMessage.class)
public interface NotificationDAO {
    
    @SqlUpdate("INSERT INTO notifications (id, title, message, channel, priority, status, recipient_id, " +
               "recipient_email, recipient_phone, recipient_slack_channel, scheduled_time, created_at, " +
               "updated_at, template_id, batch_id) " +
               "VALUES (:id, :title, :message, :channel, :priority, :status, :recipientId, " +
               ":recipientEmail, :recipientPhone, :recipientSlackChannel, :scheduledTime, :createdAt, " +
               ":updatedAt, :templateId, :batchId)")
    @GetGeneratedKeys
    void insertNotification(@BindBean NotificationMessage notification);
    
    @SqlQuery("SELECT * FROM notifications WHERE id = :id")
    Optional<NotificationMessage> findById(@Bind("id") String id);
    
    @SqlQuery("SELECT * FROM notifications WHERE recipient_id = :recipientId ORDER BY created_at DESC " +
              "LIMIT :limit OFFSET :offset")
    List<NotificationMessage> findByRecipientId(@Bind("recipientId") String recipientId, 
                                               @Bind("limit") int limit, 
                                               @Bind("offset") int offset);
    
    @SqlQuery("SELECT * FROM notifications WHERE status = :status ORDER BY priority DESC, created_at ASC " +
              "LIMIT :limit")
    List<NotificationMessage> findByStatus(@Bind("status") NotificationStatus status, 
                                          @Bind("limit") int limit);
    
    @SqlQuery("SELECT * FROM notifications WHERE channel = :channel AND status = :status " +
              "ORDER BY priority DESC, created_at ASC LIMIT :limit")
    List<NotificationMessage> findByChannelAndStatus(@Bind("channel") NotificationChannel channel,
                                                    @Bind("status") NotificationStatus status,
                                                    @Bind("limit") int limit);
    
    @SqlQuery("SELECT * FROM notifications WHERE batch_id = :batchId ORDER BY created_at ASC")
    List<NotificationMessage> findByBatchId(@Bind("batchId") String batchId);
    
    @SqlQuery("SELECT * FROM notifications WHERE scheduled_time <= :currentTime AND status = 'SCHEDULED' " +
              "ORDER BY priority DESC, scheduled_time ASC LIMIT :limit")
    List<NotificationMessage> findScheduledNotifications(@Bind("currentTime") Date currentTime,
                                                        @Bind("limit") int limit);
    
    @SqlQuery("SELECT * FROM notifications WHERE status = 'FAILED' AND retry_count < :maxRetries " +
              "ORDER BY priority DESC, updated_at ASC LIMIT :limit")
    List<NotificationMessage> findFailedNotificationsForRetry(@Bind("maxRetries") int maxRetries,
                                                             @Bind("limit") int limit);
    
    @SqlUpdate("UPDATE notifications SET status = :status, updated_at = :updatedAt, " +
               "error_message = :errorMessage WHERE id = :id")
    void updateStatus(@Bind("id") String id, 
                     @Bind("status") NotificationStatus status,
                     @Bind("updatedAt") Date updatedAt,
                     @Bind("errorMessage") String errorMessage);
    
    @SqlUpdate("UPDATE notifications SET status = :status, sent_at = :sentAt, updated_at = :updatedAt " +
               "WHERE id = :id")
    void markAsSent(@Bind("id") String id,
                   @Bind("status") NotificationStatus status,
                   @Bind("sentAt") Date sentAt,
                   @Bind("updatedAt") Date updatedAt);
    
    @SqlUpdate("UPDATE notifications SET retry_count = retry_count + 1, updated_at = :updatedAt " +
               "WHERE id = :id")
    void incrementRetryCount(@Bind("id") String id, @Bind("updatedAt") Date updatedAt);
    
    @SqlQuery("SELECT COUNT(*) FROM notifications WHERE recipient_id = :recipientId")
    long countByRecipientId(@Bind("recipientId") String recipientId);
    
    @SqlQuery("SELECT COUNT(*) FROM notifications WHERE status = :status")
    long countByStatus(@Bind("status") NotificationStatus status);
    
    @SqlQuery("SELECT COUNT(*) FROM notifications WHERE batch_id = :batchId")
    long countByBatchId(@Bind("batchId") String batchId);
    
    @SqlQuery("SELECT COUNT(*) FROM notifications WHERE batch_id = :batchId AND status = :status")
    long countByBatchIdAndStatus(@Bind("batchId") String batchId, @Bind("status") NotificationStatus status);
    
    @SqlUpdate("DELETE FROM notifications WHERE created_at < :cutoffDate AND status IN ('SENT', 'FAILED')")
    int deleteOldNotifications(@Bind("cutoffDate") Date cutoffDate);
}