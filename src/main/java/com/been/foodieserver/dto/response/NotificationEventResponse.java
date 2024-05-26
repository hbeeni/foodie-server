package com.been.foodieserver.dto.response;

import com.been.foodieserver.domain.Notification;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationEventResponse {

    private Long id;
    private String type;
    private String content;
    private Boolean isRead;
    private Timestamp createdAt;

    public static NotificationEventResponse of(Notification notification, String fromUserNickname) {
        return new NotificationEventResponse(notification.getId(),
                notification.getType().name(),
                String.format(notification.getType().getNotificationText(), fromUserNickname),
                notification.getIsRead(),
                notification.getCreatedAt());
    }
}
