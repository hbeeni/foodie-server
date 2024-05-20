package com.been.foodieserver.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeResponse {

    private String userLoginId;
    private Long postId;

    public static LikeResponse of(String userLoginId, Long postId) {
        return new LikeResponse(userLoginId, postId);
    }
}
