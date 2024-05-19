package com.been.foodieserver.service;

import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CustomUserDetails;
import com.been.foodieserver.dto.UserDto;
import com.been.foodieserver.dto.response.UserInfoResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;

    public void signUp(UserDto userDto) {
        if (isLoginIdExist(userDto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_ID);
        }

        if (isNicknameExist(userDto.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        if (!arePasswordsMatching(userDto.getPassword(), userDto.getConfirmPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        userRepository.save(userDto.toEntity(encoder.encode(userDto.getPassword())));
    }

    public boolean isLoginIdExist(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    public boolean isNicknameExist(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    public Optional<CustomUserDetails> searchUser(String loginId) {
        return userRepository.findByLoginId(loginId).map(CustomUserDetails::from);
    }

    public UserInfoResponse getMyInfo(String loginId) {
        return UserInfoResponse.my(getUserOrException(loginId));
    }

    public UserInfoResponse getUserInfo(String loginId) {
        return UserInfoResponse.others(getUserOrException(loginId));
    }

    public UserInfoResponse modifyMyInfo(String loginId, UserDto userDto) {
        if (userRepository.existsByNicknameAndLoginIdIsNot(userDto.getNickname(), loginId)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = getUserOrException(loginId);
        user.modifyInfo(userDto.getNickname());

        return UserInfoResponse.my(user);
    }

    public void changePassword(String loginId, String currentPassword, String newPassword, String confirmNewPassword) {
        if (!arePasswordsMatching(newPassword, confirmNewPassword)) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        User user = getUserOrException(loginId);

        if (!isCurrentPasswordCorrect(user, currentPassword)) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        user.changePassword(encoder.encode(newPassword));
    }

    public UserInfoResponse deleteUser(String loginId) {
        User user = getUserOrException(loginId);
        user.withdraw();
        return UserInfoResponse.my(user);
    }

    /**
     * 매일 3시 탈퇴한 지 30일이 지난 사용자 삭제
     */
    @Scheduled(cron = "${schedules.cron.user.delete}")
    public void deleteUsersInactiveFor30Days() {
        log.info("execute UserService.deleteUsersInactiveFor30Days");

        Timestamp thirtyDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
        int deletedCount = userRepository.deleteAllByDeletedAtBefore(thirtyDaysAgo);

        log.info("delete {} users", deletedCount);
    }

    private boolean isCurrentPasswordCorrect(User user, String currentPassword) {
        return encoder.matches(currentPassword, user.getPassword());
    }

    private boolean arePasswordsMatching(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public User getUserOrException(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
