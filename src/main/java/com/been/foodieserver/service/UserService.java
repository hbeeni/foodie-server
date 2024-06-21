package com.been.foodieserver.service;

import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CustomUserDetails;
import com.been.foodieserver.dto.SlackEventDto;
import com.been.foodieserver.dto.UserDto;
import com.been.foodieserver.dto.response.UserInfoResponse;
import com.been.foodieserver.dto.response.UserInfoWithStatisticsResponse;
import com.been.foodieserver.dto.response.UserInfoWithStatisticsResponse.UserStatistics;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.producer.SlackProducer;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.FollowRepository;
import com.been.foodieserver.repository.LikeRepository;
import com.been.foodieserver.repository.PostRepository;
import com.been.foodieserver.repository.UserRepository;
import com.been.foodieserver.repository.cache.UserCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.been.foodieserver.service.SlackService.SlackChannel;

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
    private final SlackProducer slackProducer;
    private final ImageService imageService;
    private final UserCacheRepository userCacheRepository;

    public void signUp(UserDto userDto) {
        if (isLoginIdExist(userDto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_ID);
        }

        if (isNicknameExist(userDto.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        if (arePasswordsNotMatching(userDto.getPassword(), userDto.getConfirmPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        User signUpUser = userRepository.save(userDto.toEntity(encoder.encode(userDto.getPassword())));
        userCacheRepository.save(signUpUser);

        slackProducer.send(SlackEventDto.of(SlackChannel.AUTH, "[회원가입] userId=" + signUpUser.getLoginId()));
    }

    @Transactional(readOnly = true)
    public boolean isLoginIdExist(String loginId) {
        return userCacheRepository.existsByLoginId(loginId);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameExist(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    @Transactional(readOnly = true)
    public Optional<CustomUserDetails> searchUser(String loginId) {
        return userCacheRepository.findByLoginId(loginId).map(CustomUserDetails::from);
    }

    @Transactional(readOnly = true)
    public UserInfoWithStatisticsResponse getMyInfo(String loginId) {
        User user = getUserOrException(loginId);
        UserStatistics userStatistics = getUserStatistics(loginId);
        return UserInfoWithStatisticsResponse.my(user, userStatistics);
    }

    @Transactional(readOnly = true)
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

        userCacheRepository.modify(user);
        return UserInfoResponse.my(user);
    }

    public void uploadProfileImage(String loginId, String imageName) {
        if (!StringUtils.hasText(imageName)) {
            return;
        }

        User user = getUserOrException(loginId);

        //이미 프로필 이미지가 있으면 삭제
        if (user.hasProfileImage()) {
            imageService.delete(user.getProfileImage());
        }

        user.updateProfileImage(imageName);
        userCacheRepository.modify(user);
    }

    public void changePassword(String loginId, String currentPassword, String newPassword, String confirmNewPassword) {
        if (arePasswordsNotMatching(newPassword, confirmNewPassword)) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        User user = getUserOrException(loginId);

        if (!isCurrentPasswordCorrect(user, currentPassword)) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        user.changePassword(encoder.encode(newPassword));
        userCacheRepository.modify(user);
    }

    public UserInfoResponse deleteUser(String loginId) {
        User user = getUserOrException(loginId);
        user.withdraw();

        userRepository.flush();
        userCacheRepository.deleteByLoginId(user.getLoginId());

        return UserInfoResponse.my(user);
    }

    public void deleteProfileImage(String loginId) {
        User user = getUserOrException(loginId);

        if (user.hasProfileImage()) {
            imageService.delete(user.getProfileImage());
            user.deleteProfileImage();
            userCacheRepository.modify(user);
        }
    }

    /**
     * 매일 3시 탈퇴한 지 30일이 지난 사용자 삭제
     */
    @Scheduled(cron = "${schedules.cron.user.delete}")
    public void deleteUsersInactiveFor30Days() {
        log.info("hard delete users");

        Timestamp thirtyDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
        List<Long> userLoginIdsToDelete = userRepository.findAllByDeletedAtBefore(thirtyDaysAgo);
        List<Long> postIdsToDelete = postRepository.findAllByUserIdIn(userLoginIdsToDelete);

        likeRepository.deleteByUserIdIn(userLoginIdsToDelete);
        likeRepository.deleteByPostIdIn(userLoginIdsToDelete);

        commentRepository.deleteByUserIdIn(userLoginIdsToDelete);
        commentRepository.deleteByPostIdIn(postIdsToDelete);

        postRepository.hardDeleteByPostIdIn(postIdsToDelete);

        followRepository.deleteByFollowerIdIn(userLoginIdsToDelete);
        followRepository.deleteByFolloweeIdIn(userLoginIdsToDelete);

        int deletedCount = userRepository.hardDeleteByIdIn(userLoginIdsToDelete);

        if (userLoginIdsToDelete.size() != deletedCount) {
            log.error("user deletion not successful. to be deleted: {}, deleted: {}", userLoginIdsToDelete.size(), deletedCount);
        } else {
            log.info("delete {} users", deletedCount);
        }
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

    private boolean arePasswordsNotMatching(String password, String confirmPassword) {
        return !password.equals(confirmPassword);
    }

    public User getUserOrException(String loginId) {
        return userCacheRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
