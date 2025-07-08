package com.notification.api.resources;

import com.notification.api.db.NotificationDAO;
import com.notification.common.model.NotificationMessage;
import com.notification.common.enums.NotificationChannel;
import com.notification.common.enums.NotificationStatus;
import com.notification.api.auth.NotificationPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationResourceTest {

    @Mock
    private NotificationDAO notificationDAO;
    
    @Mock
    private JedisPool jedisPool;

    private NotificationResource notificationResource;

    @BeforeEach
    void setUp() {
        notificationResource = new NotificationResource(notificationDAO, jedisPool);
    }

    @Test
    void getNotification_ReturnsNotification_WhenNotificationExists() {
        // Given
        String notificationId = "test-id";
        NotificationMessage notification = new NotificationMessage(
            "Test Title", "Test Message", NotificationChannel.EMAIL, "user123");
        notification.setId(notificationId);
        
        when(notificationDAO.findById(notificationId)).thenReturn(Optional.of(notification));

        // When
        Response response = notificationResource.getNotification(new NotificationPrincipal("testUser", "TEST"), notificationId, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(notification, response.getEntity());
    }

    @Test
    void getNotification_ReturnsNotFound_WhenNotificationDoesNotExist() {
        // Given
        String notificationId = "nonexistent-id";
        when(notificationDAO.findById(notificationId)).thenReturn(Optional.empty());

        // When
        Response response = notificationResource.getNotification(new NotificationPrincipal("testUser", "TEST"), notificationId, null);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}