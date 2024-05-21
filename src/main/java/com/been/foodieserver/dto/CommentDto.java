package com.been.foodieserver.dto;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class CommentDto {

    private final String content;

    @Builder
    private CommentDto(String content) {
        this.content = content;
    }

    public Comment toEntity(Post post, User user) {
        return Comment.of(post, user, content);
    }
}
