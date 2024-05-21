package com.been.foodieserver.dto.response;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostResponse {

    private Long postId;
    private Writer writer;
    private String categoryName;
    private String title;
    private String content;
    private int likeCount;
    private int commentCount;
    private Timestamp createdAt;
    private Timestamp modifiedAt;
    private Timestamp deletedAt;

    public static PostResponse of(Post post) {
        return of(post.getUser(), post.getCategory(), post);
    }

    public static PostResponse of(User user, Category category, Post post) {
        return new PostResponse(post.getId(),
                Writer.of(user),
                category.getName(),
                post.getTitle(),
                post.getContent(),
                post.getLikes().size(),
                post.getComments().size(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                post.getDeletedAt());
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Writer {

        private String loginId;
        private String nickname;
        private String role;

        public static Writer of(User user) {
            return new Writer(user.getLoginId(), user.getNickname(), user.getRole().getRoleName());
        }
    }
}
