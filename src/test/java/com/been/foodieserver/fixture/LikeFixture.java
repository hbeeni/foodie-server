package com.been.foodieserver.fixture;

import com.been.foodieserver.domain.Like;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

public class LikeFixture {

    public static Like get(Long likeId, Long userId, String loginId, Long postId) {
        User user = UserFixture.get(userId, loginId);
        Post post = PostFixture.get(postId, "title", "writer", "category");
        Like like = Like.of(user, post);

        ReflectionTestUtils.setField(like, "id", likeId);

        return like;
    }
}
