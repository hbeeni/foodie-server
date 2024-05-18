package com.been.foodieserver.dto;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PostDto {

    private final Long id;
    private final Long userId;
    private final Long categoryId;
    private final String title;
    private final String content;

    @Builder
    private PostDto(Long id, Long userId, Long categoryId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
    }

    public Post toEntity(User user, Category category) {
        return Post.of(user, category, title, content);
    }
}
