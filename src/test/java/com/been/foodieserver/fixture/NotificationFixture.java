package com.been.foodieserver.fixture;

import com.been.foodieserver.domain.Notification;
import com.been.foodieserver.domain.NotificationType;
import com.been.foodieserver.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

public class NotificationFixture {

    public static Notification get(NotificationType type, long fromUserId, long targetId) {
        User receiver = UserFixture.get(1L, "receiver");
        return get(1L, receiver, type, fromUserId, targetId);
    }

    public static Notification get(Long notificationId, User receiver, NotificationType type, long fromUserId, long targetId) {
        Notification notification = Notification.of(receiver, type, fromUserId, targetId, false);

        ReflectionTestUtils.setField(notification, "id", notificationId);

        return notification;
    }
}
