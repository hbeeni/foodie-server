package com.been.foodieserver.dto.response;

import com.been.foodieserver.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInfoWithStatisticsResponse {

    @JsonUnwrapped
    private UserInfoResponse info;
    private UserStatistics statistics;

    public static UserInfoWithStatisticsResponse my(User user, UserStatistics statistics) {
        return new UserInfoWithStatisticsResponse(UserInfoResponse.my(user), statistics);
    }

    public static UserInfoWithStatisticsResponse others(User user, UserStatistics statistics) {
        return new UserInfoWithStatisticsResponse(UserInfoResponse.others(user), statistics);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserStatistics {

        private int followingCount; //유저가 팔로우한 사람 수
        private int followerCount; //유저를 팔로우한 사람 수
        private int postCount;

        /**
         * @param followingCount 유저가 팔로우한 사람 수
         * @param followerCount  유저를 팔로우한 사람 수
         * @param postCount      유저가 작성한 게시글 수
         */
        public static UserStatistics of(int followingCount, int followerCount, int postCount) {
            return new UserStatistics(followingCount, followerCount, postCount);
        }
    }
}
