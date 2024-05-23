package com.been.foodieserver.service;

import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CustomUserDetails;
import com.been.foodieserver.dto.UserDto;
import com.been.foodieserver.dto.response.UserInfoResponse;
import com.been.foodieserver.dto.response.UserInfoWithStatisticsResponse;
import com.been.foodieserver.dto.response.UserInfoWithStatisticsResponse.UserStatistics;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.FollowRepository;
import com.been.foodieserver.repository.LikeRepository;
import com.been.foodieserver.repository.PostRepository;
import com.been.foodieserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

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

    public UserInfoWithStatisticsResponse getMyInfo(String loginId) {
        User user = getUserOrException(loginId);
        UserStatistics userStatistics = getUserStatistics(loginId);
        return UserInfoWithStatisticsResponse.my(user, userStatistics);
    }

    public UserInfoWithStatisticsResponse getUserInfo(String loginId) {
        User user = getUserOrException(loginId);
        UserStatistics userStatistics = getUserStatistics(loginId);
        return UserInfoWithStatisticsResponse.others(user, userStatistics);
    }

    public UserInfoResponse modifyMyInfo(String loginId, UserDto userDto) {
        if (userRepository.existsByNicknameAndLoginIdIsNot(userDto.getNickname(), loginId)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = getUserOrException(loginId);
        user.modifyInfo(userDto.getNickname());

        userRepository.flush();

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

        userRepository.flush();

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

    private UserStatistics getUserStatistics(String loginId) {
        int followingCount = followRepository.countByFollower_LoginId(loginId);
        int followerCount = followRepository.countByFollowee_LoginId(loginId);
        int postCount = postRepository.countByUser_LoginId(loginId);
        return UserStatistics.of(followingCount, followerCount, postCount);
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
