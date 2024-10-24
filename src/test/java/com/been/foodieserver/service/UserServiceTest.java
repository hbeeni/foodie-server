package com.been.foodieserver.service;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.SlackEventDto;
import com.been.foodieserver.dto.UserDto;
import com.been.foodieserver.dto.response.UserInfoResponse;
import com.been.foodieserver.dto.response.UserInfoWithStatisticsResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.producer.SlackProducer;
import com.been.foodieserver.repository.FollowRepository;
import com.been.foodieserver.repository.PostRepository;
import com.been.foodieserver.repository.UserRepository;
import com.been.foodieserver.repository.cache.UserCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCacheRepository userCacheRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private SlackProducer slackProducer;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    private User user;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .loginId("user1")
                .password("password12")
                .confirmPassword("password12")
                .nickname("user1")
                .build();
        user = User.of(userDto.getLoginId(), "encodedPwd", userDto.getNickname(), null, Role.USER);
    }

    @DisplayName("회원 정보가 유효하면 회원가입 성공")
    @Test
    void signUp_IfUserIsValid() {
        //Given
        given(userCacheRepository.existsByLoginId(userDto.getLoginId())).willReturn(false);
        given(userRepository.existsByNickname(userDto.getNickname())).willReturn(false);
        given(encoder.encode(userDto.getPassword())).willReturn(user.getPassword());
        given(userRepository.save(user)).willReturn(mock(User.class));
        willDoNothing().given(userCacheRepository).save(any(User.class));
        willDoNothing().given(slackProducer).send(any(SlackEventDto.class));

        //When
        userService.signUp(userDto);

        //Then
        verify(userCacheRepository).existsByLoginId(user.getLoginId());
        verify(userRepository).existsByNickname(user.getNickname());
        verify(encoder).encode(anyString());
        verify(userRepository).save(user);
        verify(userCacheRepository).save(any(User.class));
        verify(slackProducer).send(any(SlackEventDto.class));
    }

    @DisplayName("아이디가 중복되면 회원가입 실패")
    @Test
    void FailToSignUp_IfLoginIdIsDuplicated() {
        //Given
        given(userCacheRepository.existsByLoginId(userDto.getLoginId())).willReturn(true);

        //When & Then
        assertThatThrownBy(() -> userService.signUp(userDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_ID.getMessage());

        verify(userCacheRepository).existsByLoginId(userDto.getLoginId());
    }

    @DisplayName("닉네임이 중복되면 회원가입 실패")
    @Test
    void FailToSignUp_IfNicknameIsDuplicated() {
        //Given
        given(userCacheRepository.existsByLoginId(userDto.getLoginId())).willReturn(false);
        given(userRepository.existsByNickname(userDto.getNickname())).willReturn(true);

        //When & Then
        assertThatThrownBy(() -> userService.signUp(userDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_NICKNAME.getMessage());

        verify(userCacheRepository).existsByLoginId(userDto.getLoginId());
        verify(userRepository).existsByNickname(userDto.getNickname());
    }

    @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않으면 회원가입 실패")
    @Test
    void FailToSignUp_IfPasswordAndPasswordConfirmationDontMatch() {
        //Given
        UserDto userDto = UserDto.builder()
                .loginId(user.getLoginId())
                .password(user.getPassword())
                .confirmPassword("confirmPwd")
                .nickname(user.getNickname())
                .build();

        given(userCacheRepository.existsByLoginId(userDto.getLoginId())).willReturn(false);
        given(userRepository.existsByNickname(userDto.getNickname())).willReturn(false);

        //When & Then
        assertThatThrownBy(() -> userService.signUp(userDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PASSWORD_CONFIRM_MISMATCH.getMessage());

        verify(userCacheRepository).existsByLoginId(userDto.getLoginId());
        verify(userRepository).existsByNickname(userDto.getNickname());
    }

    @DisplayName("아이디 중복 체크 성공")
    @Test
    void checkIfLoginIdIsDuplicated() {
        //Given
        String loginId = "user";

        given(userCacheRepository.existsByLoginId(loginId)).willReturn(false);

        //When
        boolean result = userService.isLoginIdExist(loginId);

        //Then
        assertThat(result).isFalse();
        verify(userCacheRepository).existsByLoginId(loginId);

    }

    @DisplayName("닉네임 중복 체크 성공")
    @Test
    void checkIfNicknameIsDuplicated() {
        //Given
        String nickname = "nick";

        given(userRepository.existsByNickname(nickname)).willReturn(false);

        //When
        boolean result = userService.isNicknameExist(nickname);

        //Then
        assertThat(result).isFalse();
    }

    @DisplayName("아이디가 존재하면 내 정보 조회 성공")
    @Test
    void getMyInformation_ifLoginIdExists() {
        //Given
        String loginId = userDto.getLoginId();
        int statisticsValue = 1;

        given(userCacheRepository.findByLoginId(loginId)).willReturn(Optional.of(user));
        given(followRepository.countByFollower_LoginId(loginId)).willReturn(statisticsValue);
        given(followRepository.countByFollowee_LoginId(loginId)).willReturn(statisticsValue);
        given(postRepository.countByUser_LoginId(loginId)).willReturn(statisticsValue);

        //When
        UserInfoWithStatisticsResponse result = userService.getMyInfo(loginId);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getInfo().getLoginId()).isEqualTo(loginId);
        assertThat(result.getInfo().getNickname()).isEqualTo(user.getNickname());
        assertThat(result.getInfo().getRole()).isEqualTo(user.getRole().getRoleName());
        assertThat(result.getInfo().getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(result.getInfo().getModifiedAt()).isEqualTo(user.getModifiedAt());
        assertThat(result.getStatistics().getFollowingCount()).isEqualTo(statisticsValue);

        then(userCacheRepository).should().findByLoginId(loginId);
        then(followRepository).should().countByFollower_LoginId(loginId);
        then(followRepository).should().countByFollowee_LoginId(loginId);
        then(postRepository).should().countByUser_LoginId(loginId);
    }

    @DisplayName("아이디가 존재하지 않으면 내 정보 조회 실패")
    @Test
    void failToGetMyInformation_ifLoginIdDoesntExist() {
        //Given
        String loginId = userDto.getLoginId();

        given(userCacheRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> userService.getMyInfo(loginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        then(userCacheRepository).should().findByLoginId(loginId);
        then(followRepository).shouldHaveNoInteractions();
        then(postRepository).shouldHaveNoInteractions();
    }

    @DisplayName("아이디가 존재하면 다른 유저 정보 조회 성공")
    @Test
    void getUserInformation_ifLoginIdExists() {
        //Given
        String loginId = userDto.getLoginId();
        int statisticsValue = 1;

        given(userCacheRepository.findByLoginId(loginId)).willReturn(Optional.of(user));
        given(followRepository.countByFollower_LoginId(loginId)).willReturn(statisticsValue);
        given(followRepository.countByFollowee_LoginId(loginId)).willReturn(statisticsValue);
        given(postRepository.countByUser_LoginId(loginId)).willReturn(statisticsValue);

        //When
        UserInfoWithStatisticsResponse result = userService.getUserInfo(loginId);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getInfo().getLoginId()).isEqualTo(loginId);
        assertThat(result.getInfo().getNickname()).isEqualTo(user.getNickname());
        assertThat(result.getInfo().getRole()).isNull();
        assertThat(result.getInfo().getCreatedAt()).isNull();
        assertThat(result.getInfo().getModifiedAt()).isNull();
        assertThat(result.getInfo().getDeletedAt()).isNull();
        assertThat(result.getStatistics().getFollowingCount()).isEqualTo(statisticsValue);

        then(userCacheRepository).should().findByLoginId(loginId);
        then(followRepository).should().countByFollower_LoginId(loginId);
        then(followRepository).should().countByFollowee_LoginId(loginId);
        then(postRepository).should().countByUser_LoginId(loginId);
    }

    @DisplayName("아이디가 존재하지 않으면 다른 유저 정보 조회 실패")
    @Test
    void failToGetUserInformation_ifLoginIdDoesntExist() {
        //Given
        String loginId = userDto.getLoginId();

        given(userCacheRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> userService.getUserInfo(loginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        then(userCacheRepository).should().findByLoginId(loginId);
        then(followRepository).shouldHaveNoInteractions();
        then(postRepository).shouldHaveNoInteractions();
    }

    @DisplayName("닉네임 수정 시 다른 유저의 닉네임과 중복되지 않으면 변경 성공")
    @Test
    void modifyNickname_IfNicknameIsNotDuplicated() {
        //Given
        String loginId = user.getLoginId();
        UserDto userDto = UserDto.builder()
                .nickname("modified")
                .build();

        given(userRepository.existsByNicknameAndLoginIdIsNot(userDto.getNickname(), loginId)).willReturn(false);
        given(userCacheRepository.findByLoginId(loginId)).willReturn(Optional.of(user));
        willDoNothing().given(userRepository).flush();
        willDoNothing().given(userCacheRepository).modify(user);

        //When
        UserInfoResponse result = userService.modifyMyInfo(loginId, userDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(userDto.getNickname());

        then(userRepository).should().existsByNicknameAndLoginIdIsNot(userDto.getNickname(), loginId);
        then(userCacheRepository).should().findByLoginId(loginId);
        then(userRepository).should().flush();
        then(userCacheRepository).should().modify(user);
    }

    @DisplayName("닉네임 수정 시 다른 유저의 닉네임과 중복되면 예외 발생")
    @Test
    void throwException_IfNicknameIsDuplicated() {
        //Given
        String loginId = user.getLoginId();
        UserDto userDto = UserDto.builder()
                .nickname("modified")
                .build();

        given(userRepository.existsByNicknameAndLoginIdIsNot(userDto.getNickname(), loginId)).willReturn(true);

        //When & Then
        assertThatThrownBy(() -> userService.modifyMyInfo(loginId, userDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_NICKNAME.getMessage());

        then(userRepository).should().existsByNicknameAndLoginIdIsNot(userDto.getNickname(), loginId);
        then(userRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("비밀번호 변경 시 요청이 유효하면 비밀번호 변경 성공")
    @Test
    void changePassword_IfRequestIsValid() {
        //Given
        String userPassword = "current12";
        String currentPassword = "current12";
        String newPassword = "newpwd12";
        String encodedPassword = "encodedpwd12";
        User user = User.of("user1", userPassword, "nickname", null, Role.USER);

        given(userCacheRepository.findByLoginId(user.getLoginId())).willReturn(Optional.of(user));
        given(encoder.matches(currentPassword, userPassword)).willReturn(true);
        given(encoder.encode(newPassword)).willReturn(encodedPassword);

        //When
        userService.changePassword(user.getLoginId(), currentPassword, newPassword, newPassword);

        //Then
        assertThat(user).hasFieldOrPropertyWithValue("password", encodedPassword);

        then(userCacheRepository).should().findByLoginId(user.getLoginId());
        then(encoder).should().matches(currentPassword, userPassword);
        then(encoder).should().encode(newPassword);
    }

    @DisplayName("비밀번호 변경 시 새로운 비밀번호와 새로운 비밀번호 확인이 다르면 예외 발생")
    @Test
    void throwException_IfNewPasswordAndConfirmNewPasswordAreDifferent() {
        //Given
        String currentPassword = "current12";
        String newPassword = "newpwd12";
        String confirmNewPassword = "confirmpwd12";

        //When & Then
        assertThatThrownBy(() -> userService.changePassword("user1", currentPassword, newPassword, confirmNewPassword))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PASSWORD_CONFIRM_MISMATCH.getMessage());

        then(userRepository).shouldHaveNoInteractions();
    }

    @DisplayName("비밀번호 변경 시 현재 비밀번호가 틀리면 예외 발생")
    @Test
    void throwException_IfCurrentPasswordIsIncorrect() {
        //Given
        String loginId = "user1";
        String currentPassword = "different12";
        String newPassword = "newpwd12";
        User user = User.of(loginId, "current12", "nickname", null, Role.USER);

        given(userCacheRepository.findByLoginId(user.getLoginId())).willReturn(Optional.of(user));
        given(encoder.matches(currentPassword, user.getPassword())).willReturn(false);

        //When & Then
        assertThatThrownBy(() -> userService.changePassword(loginId, currentPassword, newPassword, newPassword))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_PASSWORD.getMessage());

        assertThat(user).hasFieldOrPropertyWithValue("password", user.getPassword());

        then(userCacheRepository).should().findByLoginId(user.getLoginId());
        then(encoder).should().matches(currentPassword, user.getPassword());
    }

    @DisplayName("회원 탈퇴 시 탈퇴 날짜 입력")
    @Test
    void setDeletionDate_WhenDeletingUser() {
        //Given
        String loginId = user.getLoginId();

        when(userCacheRepository.findByLoginId(loginId)).thenReturn(Optional.of(user));
        willDoNothing().given(userRepository).flush();
        willDoNothing().given(userCacheRepository).deleteByLoginId(loginId);

        //When
        UserInfoResponse result = userService.deleteUser(loginId);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(userDto.getNickname());
        assertThat(result.getDeletedAt()).isNotNull();

        then(userCacheRepository).should().findByLoginId(loginId);
        then(userRepository).should().flush();
        then(userCacheRepository).should().deleteByLoginId(loginId);
    }
}
