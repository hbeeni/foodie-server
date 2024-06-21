package com.been.foodieserver.service;

import com.been.foodieserver.domain.Like;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.NotificationEventDto;
import com.been.foodieserver.dto.response.LikeResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.fixture.LikeFixture;
import com.been.foodieserver.fixture.PostFixture;
import com.been.foodieserver.fixture.UserFixture;
import com.been.foodieserver.producer.NotificationProducer;
import com.been.foodieserver.repository.LikeRepository;
import com.been.foodieserver.repository.cache.LikeCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private LikeCacheRepository likeCacheRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private PostLikeService postLikeService;

    private Post post;
    private User likingUser;
    private Like like;

    @BeforeEach
    void setUp() {
        long postId = 1L;
        long userId = 2L;
        String loginId = "user";

        post = PostFixture.get(postId, "title", "writer", "자유 게시판");
        likingUser = UserFixture.get(userId, loginId);
        like = LikeFixture.get(1L, userId, loginId, postId);
    }

    @DisplayName("좋아요 요청이 유효하면 게시글 좋아요 성공")
    @Test
    void likePost_IfRequestIsValid() {
        //Given
        long postId = post.getId();
        String loginId = likingUser.getLoginId();

        given(likeCacheRepository.existsByUserLoginIdAndPostId(loginId, postId)).willReturn(false);
        given(postService.getPostWithFetchJoinOrException(postId)).willReturn(post);
        given(userService.getUserOrException(loginId)).willReturn(likingUser);
        given(likeRepository.save(any(Like.class))).willReturn(like);
        willDoNothing().given(likeCacheRepository).save(any(Like.class));
        willDoNothing().given(notificationProducer).send(any(NotificationEventDto.class));

        //When
        LikeResponse result = postLikeService.like(loginId, postId);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getUserLoginId()).isEqualTo(loginId);
        assertThat(result.getPostId()).isEqualTo(postId);

        then(likeCacheRepository).should().existsByUserLoginIdAndPostId(loginId, postId);
        then(postService).should().getPostWithFetchJoinOrException(postId);
        then(userService).should().getUserOrException(loginId);
        then(likeRepository).should().save(any(Like.class));
        then(likeCacheRepository).should().save(any(Like.class));
        then(notificationProducer).should().send(any(NotificationEventDto.class));
    }

    @DisplayName("좋아요 시 좋아요 한 게시글을 또 좋아요 하면 예외 발생")
    @Test
    void throwsException_IfAlreadyLikedPost() {
        //Given
        long postId = post.getId();
        String loginId = post.getUser().getLoginId();

        given(likeCacheRepository.existsByUserLoginIdAndPostId(loginId, postId)).willReturn(true);

        //When
        assertThatThrownBy(() -> postLikeService.like(loginId, postId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.ALREADY_LIKED.getMessage());

        //Then
        then(likeCacheRepository).should().existsByUserLoginIdAndPostId(loginId, postId);
        then(postService).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
        then(notificationProducer).shouldHaveNoInteractions();
    }

    @DisplayName("좋아요 시 좋아요한 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist() {
        //Given
        long postId = post.getId();
        String loginId = likingUser.getLoginId();

        given(likeCacheRepository.existsByUserLoginIdAndPostId(loginId, postId)).willReturn(false);
        given(postService.getPostWithFetchJoinOrException(postId)).willThrow(new CustomException(ErrorCode.POST_NOT_FOUND));

        //When
        assertThatThrownBy(() -> postLikeService.like(loginId, postId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(likeCacheRepository).should().existsByUserLoginIdAndPostId(loginId, postId);
        then(postService).should().getPostWithFetchJoinOrException(postId);
        then(userService).shouldHaveNoInteractions();
        then(notificationProducer).shouldHaveNoInteractions();
    }

    @DisplayName("좋아요 시 자신의 글을 좋아요하면 예외 발생")
    @Test
    void throwsException_IfUserLikesOwnPost() {
        //Given
        long postId = post.getId();
        String loginId = post.getUser().getLoginId();

        given(likeCacheRepository.existsByUserLoginIdAndPostId(loginId, postId)).willReturn(false);
        given(postService.getPostWithFetchJoinOrException(postId)).willReturn(post);

        //When
        assertThatThrownBy(() -> postLikeService.like(loginId, postId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.LIKE_OWN_POST.getMessage());

        //Then
        then(likeCacheRepository).should().existsByUserLoginIdAndPostId(loginId, postId);
        then(postService).should().getPostWithFetchJoinOrException(postId);
        then(userService).shouldHaveNoInteractions();
        then(notificationProducer).shouldHaveNoInteractions();
    }

    @DisplayName("좋아요 취소 요청이 유효하면 게시글 좋아요 취소 성공")
    @Test
    void unlikePost_IfRequestIsValid() {
        //Given
        long postId = post.getId();
        String loginId = likingUser.getLoginId();

        when(likeRepository.deleteByUserLoginIdAndPostId(loginId, postId)).thenReturn(1);
        willDoNothing().given(likeCacheRepository).deleteByUserLoginIdAndPostId(loginId, postId);

        //When
        postLikeService.unlike(loginId, postId);

        //Then
        then(likeRepository).should().deleteByUserLoginIdAndPostId(loginId, postId);
        then(likeCacheRepository).should().deleteByUserLoginIdAndPostId(loginId, postId);
    }

    @DisplayName("좋아요 취소 시 취소하려는 좋아요가 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfLikeNotFound() {
        //Given
        long postId = post.getId();
        String loginId = likingUser.getLoginId();

        when(likeRepository.deleteByUserLoginIdAndPostId(loginId, postId)).thenReturn(0);

        //When & Then
        assertThatThrownBy(() -> postLikeService.unlike(loginId, postId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.LIKE_NOT_FOUND.getMessage());

        then(likeRepository).should().deleteByUserLoginIdAndPostId(loginId, postId);
        then(likeCacheRepository).shouldHaveNoInteractions();
    }
}
