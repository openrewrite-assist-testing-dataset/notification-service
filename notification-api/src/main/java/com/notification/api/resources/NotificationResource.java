package com.notification.api.resources;

import com.notification.api.auth.NotificationPrincipal;
import com.notification.api.db.NotificationDAO;
import com.notification.common.model.NotificationMessage;
import com.notification.common.enums.NotificationStatus;
import com.notification.common.enums.NotificationChannel;
import com.notification.common.util.DateTimeUtil;
import io.dropwizard.auth.Auth;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Jedis;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/v1/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {
    
    private final NotificationDAO notificationDAO;
    private final JedisPool jedisPool;
    
    public NotificationResource(NotificationDAO notificationDAO, JedisPool jedisPool) {
        this.notificationDAO = notificationDAO;
        this.jedisPool = jedisPool;
    }
    
    @POST
    public Response createNotification(@Auth NotificationPrincipal user,
                                     @Valid @NotNull NotificationMessage notification,
                                     @Context UriInfo uriInfo) {
        try {
            // Generate ID if not provided
            if (notification.getId() == null) {
                notification.setId(UUID.randomUUID().toString());
            }
            
            // Set defaults if not provided
            if (notification.getCreatedAt() == null) {
                notification.setCreatedAt(DateTimeUtil.getCurrentTime());
            }
            if (notification.getUpdatedAt() == null) {
                notification.setUpdatedAt(DateTimeUtil.getCurrentTime());
            }
            if (notification.getStatus() == null) {
                notification.setStatus(NotificationStatus.PENDING);
            }
            
            // Insert into database
            notificationDAO.insertNotification(notification);
            
            // Queue for processing using Redis
            try (Jedis jedis = jedisPool.getResource()) {
                String queueKey = "notification_queue:" + notification.getChannel().getValue();
                jedis.lpush(queueKey, notification.getId());
            }
            
            return Response.status(Response.Status.CREATED)
                .entity(notification)
                .build();
                
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to create notification: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @GET
    @Path("/{id}")
    public Response getNotification(@Auth NotificationPrincipal user,
                                  @PathParam("id") @NotNull String id,
                                  @Context UriInfo uriInfo) {
        try {
            Optional<NotificationMessage> notification = notificationDAO.findById(id);
            
            if (notification.isPresent()) {
                return Response.ok(notification.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Notification not found with id: " + id + "\"}")
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve notification: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @GET
    @Path("/recipient/{recipientId}")
    public Response getNotificationsByRecipient(@Auth NotificationPrincipal user,
                                              @PathParam("recipientId") @NotNull String recipientId,
                                              @DefaultValue("0") @QueryParam("offset") int offset,
                                              @DefaultValue("20") @QueryParam("limit") int limit,
                                              @Context UriInfo uriInfo) {
        try {
            List<NotificationMessage> notifications = notificationDAO.findByRecipientId(recipientId, limit, offset);
            long totalCount = notificationDAO.countByRecipientId(recipientId);
            
            return Response.ok(notifications)
                .header("X-Total-Count", totalCount)
                .header("X-Offset", offset)
                .header("X-Limit", limit)
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve notifications: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @GET
    @Path("/status/{status}")
    public Response getNotificationsByStatus(@Auth NotificationPrincipal user,
                                           @PathParam("status") @NotNull String statusStr,
                                           @DefaultValue("100") @QueryParam("limit") int limit,
                                           @Context UriInfo uriInfo) {
        try {
            NotificationStatus status = NotificationStatus.fromValue(statusStr);
            List<NotificationMessage> notifications = notificationDAO.findByStatus(status, limit);
            long totalCount = notificationDAO.countByStatus(status);
            
            return Response.ok(notifications)
                .header("X-Total-Count", totalCount)
                .header("X-Limit", limit)
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Invalid status: " + statusStr + "\"}")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve notifications: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @PUT
    @Path("/{id}/status")
    public Response updateNotificationStatus(@Auth NotificationPrincipal user,
                                           @PathParam("id") @NotNull String id,
                                           @FormParam("status") @NotNull String statusStr,
                                           @FormParam("errorMessage") String errorMessage,
                                           @Context UriInfo uriInfo) {
        try {
            NotificationStatus status = NotificationStatus.fromValue(statusStr);
            
            // Check if notification exists
            Optional<NotificationMessage> existing = notificationDAO.findById(id);
            if (!existing.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Notification not found with id: " + id + "\"}")
                    .build();
            }
            
            Date now = DateTimeUtil.getCurrentTime();
            
            if (status == NotificationStatus.SENT) {
                notificationDAO.markAsSent(id, status, now, now);
            } else {
                notificationDAO.updateStatus(id, status, now, errorMessage);
            }
            
            return Response.ok("{\"message\": \"Status updated successfully\"}").build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Invalid status: " + statusStr + "\"}")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to update notification status: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @POST
    @Path("/{id}/retry")
    public Response retryNotification(@Auth NotificationPrincipal user,
                                    @PathParam("id") @NotNull String id,
                                    @Context UriInfo uriInfo) {
        try {
            Optional<NotificationMessage> notification = notificationDAO.findById(id);
            
            if (!notification.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Notification not found with id: " + id + "\"}")
                    .build();
            }
            
            NotificationMessage notif = notification.get();
            
            // Check if notification can be retried
            if (notif.getStatus() != NotificationStatus.FAILED) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Only failed notifications can be retried\"}")
                    .build();
            }
            
            // Update status to pending and increment retry count
            notificationDAO.updateStatus(id, NotificationStatus.PENDING, DateTimeUtil.getCurrentTime(), null);
            notificationDAO.incrementRetryCount(id, DateTimeUtil.getCurrentTime());
            
            // Re-queue for processing
            try (Jedis jedis = jedisPool.getResource()) {
                String queueKey = "notification_queue:" + notif.getChannel().getValue();
                jedis.lpush(queueKey, id);
            }
            
            return Response.ok("{\"message\": \"Notification queued for retry\"}").build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retry notification: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @GET
    @Path("/stats")
    public Response getNotificationStats(@Auth NotificationPrincipal user,
                                       @Context UriInfo uriInfo) {
        try {
            long pendingCount = notificationDAO.countByStatus(NotificationStatus.PENDING);
            long processingCount = notificationDAO.countByStatus(NotificationStatus.PROCESSING);
            long sentCount = notificationDAO.countByStatus(NotificationStatus.SENT);
            long failedCount = notificationDAO.countByStatus(NotificationStatus.FAILED);
            long scheduledCount = notificationDAO.countByStatus(NotificationStatus.SCHEDULED);
            
            String stats = String.format(
                "{\"pending\": %d, \"processing\": %d, \"sent\": %d, \"failed\": %d, \"scheduled\": %d}",
                pendingCount, processingCount, sentCount, failedCount, scheduledCount
            );
            
            return Response.ok(stats).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve stats: " + e.getMessage() + "\"}")
                .build();
        }
    }
}