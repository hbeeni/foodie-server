package com.been.foodieserver.dto.response;

import com.been.foodieserver.domain.Follow;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowerResponse {

    private String loginId;
    private String nickname;
    private Timestamp followStartDate;

    public static FollowerResponse of(Follow follow) {
        return new FollowerResponse(follow.getFollower().getLoginId(),
                follow.getFollower().getNickname(),
                follow.getCreatedAt());
    }
}
