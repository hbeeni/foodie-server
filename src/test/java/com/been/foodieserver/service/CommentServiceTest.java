package com.been.foodieserver.service;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CommentDto;
import com.been.foodieserver.dto.response.CommentResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.fixture.CommentFixture;
import com.been.foodieserver.fixture.PostFixture;
import com.been.foodieserver.fixture.UserFixture;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment comment;
    private Post post;
    private User user;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        post = PostFixture.get("title", "user1", "자유 게시판");
        user = UserFixture.get(2L, "user2");
        comment = CommentFixture.get(user, post, 1L, "comment content");
        commentDto = CommentDto.builder()
                .content(comment.getContent())
                .build();
    }

    @DisplayName("댓글 요청이 유효하면 게시글 작성 성공")
    @Test
    void writeComment_IfRequestIsValid() {
        //Given
        given(postRepository.findWithFetchJoinById(post.getId())).willReturn(Optional.of(post));
        given(userService.getUserOrException(user.getLoginId())).willReturn(user);
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        //When
        CommentResponse result = commentService.writeComment(user.getLoginId(), post.getId(), commentDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(comment.getContent());
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getCategoryName()).isEqualTo(post.getCategory().getName());
        assertThat(result.getWriter().getLoginId()).isEqualTo(user.getLoginId());

        then(postRepository).should().findWithFetchJoinById(post.getId());
        then(userService).should().getUserOrException(user.getLoginId());
        then(commentRepository).should().save(any(Comment.class));
    }

    @DisplayName("댓글 작성 시 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist() {
        //Given
        Long postId = post.getId();
        String loginId = user.getLoginId();

        given(postRepository.findWithFetchJoinById(postId)).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> commentService.writeComment(loginId, postId, commentDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(postRepository).should().findWithFetchJoinById(postId);
        then(userService).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoInteractions();
    }
}
