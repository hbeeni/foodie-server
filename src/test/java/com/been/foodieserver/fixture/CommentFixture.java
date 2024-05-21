package com.been.foodieserver.fixture;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

public class CommentFixture {

    public static Comment get(User user, Post post, Long commentId, String content) {
        Comment comment = Comment.of(post, user, content);
        ReflectionTestUtils.setField(comment, "id", commentId);
        return comment;
    }
}
