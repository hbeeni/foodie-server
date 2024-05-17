package com.been.foodieserver.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowResponse {

    public static final String STATUS_FOLLOW = "follow";
    public static final String STATUS_UNFOLLOW = "unfollow";

    private String status;
    private String follower;
    private String followee;

    public static FollowResponse follow(String followerId, String followeeId) {
        return new FollowResponse(STATUS_FOLLOW, followerId, followeeId);
    }

    public static FollowResponse unfollow(String followerId, String followeeId) {
        return new FollowResponse(STATUS_UNFOLLOW, followerId, followeeId);
    }
}
