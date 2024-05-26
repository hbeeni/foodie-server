package com.been.foodieserver.fixture;

import com.been.foodieserver.domain.Follow;
import com.been.foodieserver.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

public class FollowFixture {

    public static Follow get(Long followId, Long followerId, String followerLoginId, Long followeeId, String followeeLoginId) {
        User follower = UserFixture.get(followerId, followerLoginId);
        User followee = UserFixture.get(followeeId, followeeLoginId);

        Follow follow = Follow.of(follower, followee);
        ReflectionTestUtils.setField(follow, "id", followId);
        return follow;
    }
}
