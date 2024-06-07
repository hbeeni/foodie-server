package com.been.foodieserver.dto;

import com.been.foodieserver.domain.NotificationType;
import com.been.foodieserver.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationEventDto {

    private UserDto receiver;
    private NotificationType type;
    private UserDto fromUser;
    private Long targetId;

    public static NotificationEventDto of(User receiver, NotificationType type, User fromUser, Long targetId) {
        return new NotificationEventDto(toUserDto(receiver), type, toUserDto(fromUser), targetId);
    }

    private static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .build();
    }
}
