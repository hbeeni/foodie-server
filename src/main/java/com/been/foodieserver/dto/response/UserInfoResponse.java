package com.been.foodieserver.dto.response;

import com.been.foodieserver.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInfoResponse {

    private Long id;
    private String loginId;
    private String nickname;
    private String role;
    private Timestamp createdAt;
    private Timestamp modifiedAt;
    private Timestamp deletedAt;

    public static UserInfoResponse my(User user) {
        return new UserInfoResponse(user.getId(),
                user.getLoginId(),
                user.getNickname(),
                user.getRole().getRoleName(),
                user.getCreatedAt(),
                user.getModifiedAt(),
                user.getDeletedAt());
    }

    public static UserInfoResponse others(User user) {
        return new UserInfoResponse(user.getId(),
                null,
                user.getNickname(),
                null,
                null,
                null,
                null);
    }
}
