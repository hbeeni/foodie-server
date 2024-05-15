package com.been.foodieserver.service;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CustomUserDetails;
import com.been.foodieserver.dto.UserDto;
import com.been.foodieserver.dto.response.UserInfoResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.UserRepository;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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
        user = User.of(userDto.getLoginId(), "encodedPwd", userDto.getNickname(), Role.USER);
    }

    @DisplayName("회원 정보가 유효하면 회원가입 성공")
    @Test
    void signUp_IfUserIsValid() {
        //Given
        given(userRepository.existsByLoginId(userDto.getLoginId())).willReturn(false);
        given(userRepository.existsByNickname(userDto.getNickname())).willReturn(false);
        given(encoder.encode(userDto.getPassword())).willReturn(user.getPassword());
        given(userRepository.save(user)).willReturn(any(User.class));

        //When
        userService.signUp(userDto);

        //Then
        verify(userRepository).existsByLoginId(user.getLoginId());
        verify(userRepository).existsByNickname(user.getNickname());
        verify(encoder).encode(anyString());
        verify(userRepository).save(user);
    }

    @DisplayName("아이디가 중복되면 회원가입 실패")
    @Test
    void FailToSignUp_IfLoginIdIsDuplicated() {
        //Given
        given(userRepository.existsByLoginId(userDto.getLoginId())).willReturn(true);

        //When & Then
        assertThatThrownBy(() -> userService.signUp(userDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_ID.getMessage());
        verify(userRepository).existsByLoginId(userDto.getLoginId());
    }

    @DisplayName("닉네임이 중복되면 회원가입 실패")
    @Test
    void FailToSignUp_IfNicknameIsDuplicated() {
        //Given
        given(userRepository.existsByLoginId(userDto.getLoginId())).willReturn(false);
        given(userRepository.existsByNickname(userDto.getNickname())).willReturn(true);

        //When & Then
        assertThatThrownBy(() -> userService.signUp(userDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_NICKNAME.getMessage());
        verify(userRepository).existsByLoginId(userDto.getLoginId());
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

        given(userRepository.existsByLoginId(userDto.getLoginId())).willReturn(false);
        given(userRepository.existsByNickname(userDto.getNickname())).willReturn(false);

        //When & Then
        assertThatThrownBy(() -> userService.signUp(userDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PASSWORD_CONFIRM_MISMATCH.getMessage());
        verify(userRepository).existsByLoginId(userDto.getLoginId());
        verify(userRepository).existsByNickname(userDto.getNickname());
    }

    @DisplayName("아이디 중복 체크 성공")
    @Test
    void checkIfLoginIdIsDuplicated() {
        //Given
        String loginId = "user";

        given(userRepository.existsByLoginId(loginId)).willReturn(false);

        //When
        boolean result = userService.isLoginIdDuplicated(loginId);

        //Then
        assertThat(result).isFalse();
    }

    @DisplayName("닉네임 중복 체크 성공")
    @Test
    void checkIfNicknameIsDuplicated() {
        //Given
        String nickname = "nick";

        given(userRepository.existsByNickname(nickname)).willReturn(false);

        //When
        boolean result = userService.isNicknameDuplicated(nickname);

        //Then
        assertThat(result).isFalse();
    }

    @DisplayName("아이디가 존재하면 유저 검색 성공")
    @Test
    void searchUser_ifLoginIdExists() {
        //Given
        given(userRepository.findByLoginId(userDto.getLoginId())).willReturn(Optional.of(user));

        //When
        Optional<CustomUserDetails> result = userService.searchUser(userDto.getLoginId());

        //Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().getUsername()).isEqualTo(userDto.getLoginId());

        then(userRepository).should().findByLoginId(userDto.getLoginId());
    }

    @DisplayName("아이디가 존재하지 않으면 유저 검색 실패")
    @Test
    void failToSearchUser_ifLoginIdDoesntExist() {
        //Given
        given(userRepository.findByLoginId(userDto.getLoginId())).willReturn(Optional.empty());

        //When
        Optional<CustomUserDetails> result = userService.searchUser(userDto.getLoginId());

        //Then
        assertThat(result).isEmpty();

        then(userRepository).should().findByLoginId(userDto.getLoginId());
    }

    @DisplayName("아이디가 존재하면 내 정보 조회 성공")
    @Test
    void getMyInformation_ifLoginIdExists() {
        //Given
        String loginId = userDto.getLoginId();

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(user));

        //When
        UserInfoResponse result = userService.getMyInfo(loginId);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo(loginId);
        assertThat(result.getNickname()).isEqualTo(user.getNickname());
        assertThat(result.getRole()).isEqualTo(user.getRole().getRoleName());
        assertThat(result.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(result.getModifiedAt()).isEqualTo(user.getModifiedAt());

        then(userRepository).should().findByLoginId(loginId);
    }

    @DisplayName("아이디가 존재하지 않으면 내 정보 조회 실패")
    @Test
    void failToGetMyInformation_ifLoginIdDoesntExist() {
        //Given
        String loginId = userDto.getLoginId();

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> userService.getMyInfo(loginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        then(userRepository).should().findByLoginId(loginId);
    }

    @DisplayName("아이디가 존재하면 다른 유저 정보 조회 성공")
    @Test
    void getUserInformation_ifLoginIdExists() {
        //Given
        String loginId = userDto.getLoginId();

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(user));

        //When
        UserInfoResponse result = userService.getUserInfo(loginId);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo(loginId);
        assertThat(result.getNickname()).isEqualTo(user.getNickname());
        assertThat(result.getRole()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getModifiedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();

        then(userRepository).should().findByLoginId(loginId);
    }

    @DisplayName("아이디가 존재하지 않으면 다른 유저 정보 조회 실패")
    @Test
    void failToGetUserInformation_ifLoginIdDoesntExist() {
        //Given
        String loginId = userDto.getLoginId();

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> userService.getUserInfo(loginId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        then(userRepository).should().findByLoginId(loginId);
    }

    @DisplayName("닉네임 수정 시 다른 유저의 닉네임과 중복되지 않으면 변경함")
    @Test
    void modifyNickname_IfNicknameIsNotDuplicated() {
        //Given
        String loginId = user.getLoginId();
        UserDto userDto = UserDto.builder()
                .nickname("modified")
                .build();

        given(userRepository.existsByNicknameAndLoginIdIsNot(userDto.getNickname(), loginId)).willReturn(false);
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(user));

        //When
        UserInfoResponse result = userService.modifyMyInfo(loginId, userDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(userDto.getNickname());

        then(userRepository).should().existsByNicknameAndLoginIdIsNot(userDto.getNickname(), loginId);
        then(userRepository).should().findByLoginId(loginId);
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

    @DisplayName("비밀번호 변경 시 요청이 유효하면 비밀번호 변경")
    @Test
    void changePassword_IfRequestIsValid() {
        //Given
        String userPassword = "current12";
        String currentPassword = "current12";
        String newPassword = "newpwd12";
        String encodedPassword = "encodedpwd12";
        User user = User.of("user1", userPassword, "nickname", Role.USER);

        given(userRepository.findByLoginId(user.getLoginId())).willReturn(Optional.of(user));
        given(encoder.matches(currentPassword, userPassword)).willReturn(true);
        given(encoder.encode(newPassword)).willReturn(encodedPassword);

        //When
        userService.changePassword(user.getLoginId(), currentPassword, newPassword, newPassword);

        //Then
        assertThat(user).hasFieldOrPropertyWithValue("password", encodedPassword);

        then(userRepository).should().findByLoginId(user.getLoginId());
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
        User user = User.of(loginId, "current12", "nickname", Role.USER);

        given(userRepository.findByLoginId(user.getLoginId())).willReturn(Optional.of(user));
        given(encoder.matches(currentPassword, user.getPassword())).willReturn(false);

        //When & Then
        assertThatThrownBy(() -> userService.changePassword(loginId, currentPassword, newPassword, newPassword))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_PASSWORD.getMessage());

        assertThat(user).hasFieldOrPropertyWithValue("password", user.getPassword());

        then(userRepository).should().findByLoginId(user.getLoginId());
        then(encoder).should().matches(currentPassword, user.getPassword());
    }
}
