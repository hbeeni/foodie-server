package com.been.foodieserver.dto.response;

import com.been.foodieserver.domain.Notification;
import com.been.foodieserver.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationEventResponse {

    @JsonIgnore
    private static final String BASE_URL = "/api/v1";

    private Long id;
    private String type;
    private String content;
    private String uri;
    private Boolean isRead;
    private Timestamp createdAt;

    public static NotificationEventResponse of(Notification notification, String fromUserNickname) {
        return new NotificationEventResponse(notification.getId(),
                notification.getType().name(),
                java.lang.String.format(notification.getType().getNotificationText(), fromUserNickname),
                getUri(notification.getType(), notification.getTargetId()),
                notification.getIsRead(),
                notification.getCreatedAt());
    }

    private static String getUri(NotificationType type, Long targetId) {
        return switch (type) {
            case NEW_COMMENT_ON_POST -> BASE_URL + "/posts/" + targetId + "/comments";
            case NEW_LIKE_ON_POST -> BASE_URL + "/posts/" + targetId;
            case NEW_FOLLOW -> BASE_URL + "/follows";
        };
    }
}
