package com.been.foodieserver.fixture;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

public class PostFixture {

    public static Post get(Long postId, String title, String loginId, String categoryName) {
        return get(postId, title, title, loginId, categoryName);
    }

    public static Post get(String title, String loginId, String categoryName) {
        return get(1L, title, title, loginId, categoryName);
    }

    public static Post get(Long postId, String title, String content, String loginId, String categoryName) {
        User user = User.of(loginId, "pwd", loginId, Role.USER);
        Category category = Category.of(categoryName, null);
        Post post = Post.of(user, category, title, content);

        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(category, "id", 1L);
        ReflectionTestUtils.setField(post, "id", postId);

        return post;
    }
}
