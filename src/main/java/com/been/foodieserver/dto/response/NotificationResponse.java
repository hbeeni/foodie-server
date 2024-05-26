package com.been.foodieserver.dto.response;

import com.been.foodieserver.domain.Notification;
import com.been.foodieserver.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationResponse {

    private Long id;
    private String type;
    private String content;
    private ToUserResponse toUser;
    private Long fromUserId;
    private Long targetId;
    private Boolean isRead;

    public static NotificationResponse of(Notification notification, String fromUserNickname) {
        return new NotificationResponse(notification.getId(),
                notification.getType().name(),
                String.format(notification.getType().getNotificationText(), fromUserNickname),
                ToUserResponse.of(notification.getReceiver()),
                notification.getFromUserId(),
                notification.getTargetId(),
                notification.getIsRead());
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ToUserResponse {

        private Long id;
        private String loginId;
        private String nickname;

        public static ToUserResponse of(User user) {
            return new ToUserResponse(user.getId(), user.getLoginId(), user.getNickname());
        }
    }
}
