package com.been.foodieserver.domain.redis;

import com.been.foodieserver.domain.Post;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisPost {

    private Long id;
    private String userLoginId;
    private Long categoryId;
    private String title;
    private String content;
    private Timestamp createdAt;
    private Timestamp modifiedAt;
    private Timestamp deletedAt;

    public static RedisPost of(Post post) {
        return new RedisPost(post.getId(), post.getUser().getLoginId(), post.getCategory().getId(), post.getTitle(), post.getContent(), post.getCreatedAt(), post.getModifiedAt(), post.getDeletedAt());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof RedisPost post)) {
            return false;
        }

        return Objects.equals(getId(), post.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
