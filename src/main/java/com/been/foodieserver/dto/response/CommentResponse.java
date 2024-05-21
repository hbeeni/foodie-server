package com.been.foodieserver.dto.response;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse {

    private String categoryName;
    private Long postId;
    private Long commentId;
    private Writer writer;
    private String content;
    private Timestamp createdAt;
    private Timestamp modifiedAt;
    private Timestamp deletedAt;

    public static CommentResponse of(Comment comment) {
        return new CommentResponse(
                comment.getPost().getCategory().getName(),
                comment.getPost().getId(),
                comment.getId(),
                Writer.of(comment.getUser()),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                comment.getDeletedAt()
        );
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
