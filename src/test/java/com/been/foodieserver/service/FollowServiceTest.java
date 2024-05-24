package com.been.foodieserver.service;

import com.been.foodieserver.domain.Follow;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.response.FollowResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.fixture.UserFixture;
import com.been.foodieserver.repository.FollowRepository;
import com.been.foodieserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowService followService;

    private String followerLoginId;
    private String followeeLoginId;

    private User follower;
    private User followee;

    @BeforeEach
    void setUp() {
        followerLoginId = "follower";
        followeeLoginId = "followee";

        follower = User.of(followeeLoginId, "pwd", "nick1", Role.USER);
        followee = User.of(followeeLoginId, "pwd", "nick2", Role.USER);
    }

    @DisplayName("팔로우할 유저 로그인 아이디가 유효하면 팔로우 성공")
    @Test
    void followUser_IfLoginIdIsValid() {
        //Given
        given(followRepository.existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId)).willReturn(false);
        given(userRepository.findByLoginId(followeeLoginId)).willReturn(Optional.of(followee));
        given(userRepository.findByLoginId(followerLoginId)).willReturn(Optional.of(follower));
        given(followRepository.save(any(Follow.class))).willReturn(mock(Follow.class));

        //When
        FollowResponse result = followService.follow(followerLoginId, followeeLoginId);

        //Then
        assertThat(result.getStatus()).isEqualTo(FollowResponse.STATUS_FOLLOW);
        assertThat(result.getFollower()).isEqualTo(followerLoginId);
        assertThat(result.getFollowee()).isEqualTo(followeeLoginId);

        then(followRepository).should().existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);
        then(userRepository).should().findByLoginId(followeeLoginId);
        then(userRepository).should().findByLoginId(followerLoginId);
        then(followRepository).should().save(any(Follow.class));
    }

    @DisplayName("팔로우할 유저 로그인 아이디가 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfFollowingNonExistentUser() {
        //Given
        given(followRepository.existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId)).willReturn(false);
        given(userRepository.findByLoginId(followeeLoginId)).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> followService.follow(followerLoginId, followeeLoginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.FOLLOWEE_NOT_FOUND.getMessage());

        //Then
        then(followRepository).should().existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);
        then(userRepository).should().findByLoginId(followeeLoginId);
    }

    @DisplayName("본인을 팔로우하면 예외 발생")
    @Test
    void throwsException_IfUserFollowsSelf() {
        //Given
        String followerLoginId = "follower";
        String followeeLoginId = "follower";

        //When
        assertThatThrownBy(() -> followService.follow(followerLoginId, followeeLoginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CANNOT_FOLLOW_OR_UNFOLLOW_SELF.getMessage());

        //Then
        then(followRepository).shouldHaveNoInteractions();
        then(userRepository).shouldHaveNoInteractions();
    }

    @DisplayName("이미 팔로우한 유저를 또 팔로우하면 아무 일도 일어나지 않고 결과 반환")
    @Test
    void noAction_IfFollowAlreadyFollowingUser() {
        //Given
        given(followRepository.existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId)).willReturn(true);

        //When
        FollowResponse result = followService.follow(followerLoginId, followeeLoginId);

        //Then
        assertThat(result.getStatus()).isEqualTo(FollowResponse.STATUS_FOLLOW);
        assertThat(result.getFollower()).isEqualTo(followerLoginId);
        assertThat(result.getFollowee()).isEqualTo(followeeLoginId);

        then(followRepository).should().existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);
    }

    @DisplayName("언팔로우할 유저 로그인 아이디가 유효하면 언팔로우 성공")
    @Test
    void unfollowUser_IfLoginIdIsValid() {
        //Given
        given(followRepository.existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId)).willReturn(true);
        willDoNothing().given(followRepository).deleteByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);

        //When
        FollowResponse result = followService.unfollow(followerLoginId, followeeLoginId);

        //Then
        assertThat(result.getStatus()).isEqualTo(FollowResponse.STATUS_UNFOLLOW);
        assertThat(result.getFollower()).isEqualTo(followerLoginId);
        assertThat(result.getFollowee()).isEqualTo(followeeLoginId);

        then(followRepository).should().existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);
        then(followRepository).should().deleteByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);
    }

    @DisplayName("팔로우하지 않은 유저를 언팔로우하면 예외 발생")
    @Test
    void throwsException_IfUnfollowUserNotFollowed() {
        //Given
        given(followRepository.existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId)).willReturn(false);

        //When
        assertThatThrownBy(() -> followService.unfollow(followerLoginId, followeeLoginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.FOLLOW_NOT_FOUND.getMessage());

        //Then
        then(followRepository).should().existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);
    }

    @DisplayName("본인을 언팔로우하면 예외 발생")
    @Test
    void throwsException_IfUserUnfollowsSelf() {
        //Given
        String followerLoginId = "follower";
        String followeeLoginId = "follower";

        //When
        assertThatThrownBy(() -> followService.unfollow(followerLoginId, followeeLoginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CANNOT_FOLLOW_OR_UNFOLLOW_SELF.getMessage());

        //Then
        then(followRepository).shouldHaveNoInteractions();
        then(userRepository).shouldHaveNoInteractions();
    }

    @DisplayName("해당 사용자가 팔로우한 사용자들의 로그인 아이디 조회 성공")
    @Test
    void getMyFolloweeLoginIds_IfLoginIdIsValid() {
        //Given
        User user = UserFixture.get(1L, "follower");
        User followee1 = UserFixture.get(2L, "user1");
        User followee2 = UserFixture.get(3L, "user2");

        Follow follow1 = Follow.of(user, followee1);
        Follow follow2 = Follow.of(user, followee2);
        List<Follow> followList = List.of(follow1, follow2);

        given(followRepository.findAllWithFollowerAndFolloweeByFollower_LoginId(followerLoginId)).willReturn(followList);

        //When
        Set<String> result = followService.getFolloweeLoginIds(user.getLoginId());

        //Then
        assertThat(result).isNotNull().hasSize(followList.size());

        then(followRepository).should().findAllWithFollowerAndFolloweeByFollower_LoginId(followerLoginId);
    }

    @DisplayName("팔로우한 사용자가 없으면 빈 목록 반환")
    @Test
    void returnEmptySet_IfNoFolloweesExist() {
        //Given
        String loginId = "follower";

        given(followRepository.findAllWithFollowerAndFolloweeByFollower_LoginId(loginId)).willReturn(List.of());

        //When
        Set<String> result = followService.getFolloweeLoginIds(loginId);

        //Then
        assertThat(result).isNotNull().isEmpty();

        then(followRepository).should().findAllWithFollowerAndFolloweeByFollower_LoginId(loginId);
    }
}
