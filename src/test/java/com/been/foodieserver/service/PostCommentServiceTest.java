package com.been.foodieserver.service;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.NotificationType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private SseService sseService;

    @InjectMocks
    private PostCommentService postCommentService;

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

    @DisplayName("댓글 목록 조회 요청이 유효하면 댓글 목록 조회 성공")
    @Test
    void getCommentList_IfRequestIsValid() {
        //Given
        Long postId = post.getId();

        Comment comment2 = CommentFixture.get(user, post, 2L, "comment content2");

        List<Comment> content = List.of(comment2, comment);
        Page<Comment> commentPage = new PageImpl<>(content);

        int pageNum = 1;
        int pageSize = content.size();

        given(postRepository.existsById(postId)).willReturn(true);
        given(commentRepository.findAllWithUserAndPostAndCategoryByPostId(any(Pageable.class), eq(postId))).willReturn(commentPage);

        //When
        Page<CommentResponse> result = postCommentService.getCommentList(postId, pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getCommentId()).isEqualTo(comment2.getId());

        then(postRepository).should().existsById(postId);
        then(commentRepository).should().findAllWithUserAndPostAndCategoryByPostId(any(Pageable.class), eq(postId));
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 목록 조회 시 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist_WhenGettingCommentList() {
        //Given
        Long postId = post.getId();

        given(postRepository.existsById(postId)).willReturn(false);

        //When
        assertThatThrownBy(() -> postCommentService.getCommentList(postId, 1, 10))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(postRepository).should().existsById(postId);
        then(userService).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 작성 요청이 유효하면 댓글 작성 성공")
    @Test
    void writeComment_IfRequestIsValid() {
        //Given
        given(postRepository.findWithUserAndCategoryById(post.getId())).willReturn(Optional.of(post));
        given(userService.getUserOrException(user.getLoginId())).willReturn(user);
        given(commentRepository.save(any(Comment.class))).willReturn(comment);
        willDoNothing().given(sseService).saveNotificationAndSendToClient(post.getUser(), NotificationType.NEW_COMMENT_ON_POST, user, post.getId());

        //When
        CommentResponse result = postCommentService.writeComment(user.getLoginId(), post.getId(), commentDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(comment.getContent());
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getCategoryName()).isEqualTo(post.getCategory().getName());
        assertThat(result.getWriter().getLoginId()).isEqualTo(user.getLoginId());

        then(postRepository).should().findWithUserAndCategoryById(post.getId());
        then(userService).should().getUserOrException(user.getLoginId());
        then(commentRepository).should().save(any(Comment.class));
        then(sseService).should().saveNotificationAndSendToClient(post.getUser(), NotificationType.NEW_COMMENT_ON_POST, user, post.getId());
    }

    @DisplayName("댓글 작성 시 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist() {
        //Given
        Long postId = post.getId();
        String loginId = user.getLoginId();

        given(postRepository.findWithUserAndCategoryById(postId)).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> postCommentService.writeComment(loginId, postId, commentDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(postRepository).should().findWithUserAndCategoryById(postId);
        then(userService).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 수정 요청이 유효하면 댓글 수정 성공")
    @Test
    void modifyComment_IfRequestIsValid() {
        //Given
        given(postRepository.existsById(post.getId())).willReturn(true);
        given(commentRepository.findWithUserAndPostAndCategoryByIdAndLoginIdAndPostId(comment.getId(), user.getLoginId(), post.getId()))
                .willReturn(Optional.of(comment));

        //When
        CommentResponse result = postCommentService.modifyComment(user.getLoginId(), post.getId(), comment.getId(), commentDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(comment.getContent());
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getCategoryName()).isEqualTo(post.getCategory().getName());
        assertThat(result.getWriter().getLoginId()).isEqualTo(user.getLoginId());

        then(postRepository).should().existsById(post.getId());
        then(commentRepository).should().findWithUserAndPostAndCategoryByIdAndLoginIdAndPostId(comment.getId(), user.getLoginId(), post.getId());
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 수정 시 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist_WhenModifyingComment() {
        //Given
        String loginId = user.getLoginId();
        Long postId = post.getId();
        Long commentId = comment.getId();

        given(postRepository.existsById(post.getId())).willReturn(false);

        //When & Then
        assertThatThrownBy(() -> postCommentService.modifyComment(loginId, postId, commentId, commentDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        then(postRepository).should().existsById(post.getId());
        then(userService).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 수정 시 수정할 댓글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfCommentDoesntExist_WhenModifyingComment() {
        //Given
        String loginId = user.getLoginId();
        Long postId = post.getId();
        Long commentId = comment.getId();

        given(postRepository.existsById(post.getId())).willReturn(true);
        given(commentRepository.findWithUserAndPostAndCategoryByIdAndLoginIdAndPostId(comment.getId(), user.getLoginId(), post.getId()))
                .willReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> postCommentService.modifyComment(loginId, postId, commentId, commentDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());

        then(postRepository).should().existsById(post.getId());
        then(commentRepository).should().findWithUserAndPostAndCategoryByIdAndLoginIdAndPostId(comment.getId(), user.getLoginId(), post.getId());
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 삭제 요청이 유효하면 댓글 삭제 성공")
    @Test
    void deleteComment_IfRequestIsValid() {
        //Given
        given(postRepository.existsById(post.getId())).willReturn(true);
        given(commentRepository.deleteByIdAndPostIdAndUserLoginId(comment.getId(), post.getId(), user.getLoginId()))
                .willReturn(1);

        //When
        postCommentService.deleteComment(user.getLoginId(), post.getId(), comment.getId());

        //Then
        then(postRepository).should().existsById(post.getId());
        then(commentRepository).should().deleteByIdAndPostIdAndUserLoginId(comment.getId(), post.getId(), user.getLoginId());
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 삭제 시 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist_WhenDeletingComment() {
        //Given
        String loginId = user.getLoginId();
        Long postId = post.getId();
        Long commentId = comment.getId();

        given(postRepository.existsById(post.getId())).willReturn(false);

        //When & Then
        assertThatThrownBy(() -> postCommentService.deleteComment(loginId, postId, commentId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        then(postRepository).should().existsById(post.getId());
        then(userService).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 삭제 시 삭제할 댓글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfCommentDoesntExist_WhenDeletingComment() {
        //Given
        String loginId = user.getLoginId();
        Long postId = post.getId();
        Long commentId = comment.getId();

        given(postRepository.existsById(post.getId())).willReturn(true);
        given(commentRepository.deleteByIdAndPostIdAndUserLoginId(comment.getId(), post.getId(), user.getLoginId()))
                .willReturn(0);

        //When & Then
        assertThatThrownBy(() -> postCommentService.deleteComment(loginId, postId, commentId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());

        then(postRepository).should().existsById(post.getId());
        then(commentRepository).should().deleteByIdAndPostIdAndUserLoginId(comment.getId(), post.getId(), user.getLoginId());
        then(userService).shouldHaveNoInteractions();
    }
}
