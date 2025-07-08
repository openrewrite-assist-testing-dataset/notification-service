package com.notification.api.resources;

import com.notification.api.auth.NotificationPrincipal;
import com.notification.api.db.NotificationDAO;
import com.notification.common.model.NotificationMessage;
import com.notification.common.enums.NotificationStatus;
import com.notification.common.util.DateTimeUtil;
import io.dropwizard.auth.Auth;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Jedis;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/batches")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchResource {
    
    private final NotificationDAO notificationDAO;
    private final JedisPool jedisPool;
    
    public BatchResource(NotificationDAO notificationDAO, JedisPool jedisPool) {
        this.notificationDAO = notificationDAO;
        this.jedisPool = jedisPool;
    }
    
    @POST
    public Response createBatch(@Auth NotificationPrincipal user,
                               @Valid @NotNull List<NotificationMessage> notifications,
                               @Context UriInfo uriInfo) {
        try {
            String batchId = UUID.randomUUID().toString();
            
            for (NotificationMessage notification : notifications) {
                if (notification.getId() == null) {
                    notification.setId(UUID.randomUUID().toString());
                }
                notification.setBatchId(batchId);
                notification.setCreatedAt(DateTimeUtil.getCurrentTime());
                notification.setUpdatedAt(DateTimeUtil.getCurrentTime());
                notification.setStatus(NotificationStatus.PENDING);
                
                notificationDAO.insertNotification(notification);
                
                try (Jedis jedis = jedisPool.getResource()) {
                    String queueKey = "notification_queue:" + notification.getChannel().getValue();
                    jedis.lpush(queueKey, notification.getId());
                }
            }
            
            return Response.status(Response.Status.CREATED)
                .entity("{\"batchId\": \"" + batchId + "\", \"count\": " + notifications.size() + "}")
                .build();
                
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to create batch: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @GET
    @Path("/{batchId}")
    public Response getBatch(@Auth NotificationPrincipal user,
                           @PathParam("batchId") @NotNull String batchId,
                           @Context UriInfo uriInfo) {
        try {
            List<NotificationMessage> notifications = notificationDAO.findByBatchId(batchId);
            
            if (notifications.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Batch not found with id: " + batchId + "\"}")
                    .build();
            }
            
            return Response.ok(notifications).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve batch: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @GET
    @Path("/{batchId}/stats")
    public Response getBatchStats(@Auth NotificationPrincipal user,
                                @PathParam("batchId") @NotNull String batchId,
                                @Context UriInfo uriInfo) {
        try {
            long totalCount = notificationDAO.countByBatchId(batchId);
            long sentCount = notificationDAO.countByBatchIdAndStatus(batchId, NotificationStatus.SENT);
            long failedCount = notificationDAO.countByBatchIdAndStatus(batchId, NotificationStatus.FAILED);
            long pendingCount = notificationDAO.countByBatchIdAndStatus(batchId, NotificationStatus.PENDING);
            
            if (totalCount == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Batch not found with id: " + batchId + "\"}")
                    .build();
            }
            
            String stats = String.format(
                "{\"total\": %d, \"sent\": %d, \"failed\": %d, \"pending\": %d}",
                totalCount, sentCount, failedCount, pendingCount
            );
            
            return Response.ok(stats).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to retrieve batch stats: " + e.getMessage() + "\"}")
                .build();
        }
    }
}