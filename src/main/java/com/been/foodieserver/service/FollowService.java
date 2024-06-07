package com.been.foodieserver.service;

import com.been.foodieserver.domain.Follow;
import com.been.foodieserver.domain.NotificationType;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.NotificationEventDto;
import com.been.foodieserver.dto.response.FollowResponse;
import com.been.foodieserver.dto.response.FollowerResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.producer.NotificationProducer;
import com.been.foodieserver.repository.FollowRepository;
import com.been.foodieserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final NotificationProducer notificationProducer;

    @Transactional(readOnly = true)
    public Page<FollowerResponse> getFollowerList(String loginId, int pageNum, int pageSize) {
        Pageable pageable = makePageable(pageNum, pageSize);
        return followRepository.findAllWithFollowerAndFolloweeByFollowee_LoginId(loginId, pageable)
                .map(FollowerResponse::of);
    }

    public FollowResponse follow(String followerLoginId, String followeeLoginId) {
        if (followerLoginId.equals(followeeLoginId)) {
            throw new CustomException(ErrorCode.CANNOT_FOLLOW_OR_UNFOLLOW_SELF);
        }

        FollowResponse response = FollowResponse.follow(followerLoginId, followeeLoginId);

        if (isFollowExists(followerLoginId, followeeLoginId)) {
            return response;
        }

        User followee = getFolloweeOrException(followeeLoginId);
        User follower = getFollowerOrException(followerLoginId);

        followRepository.save(Follow.of(follower, followee));

        //event send
        notificationProducer.send(NotificationEventDto.of(followee,
                NotificationType.NEW_FOLLOW,
                follower,
                followee.getId()));

        return response;
    }

    public FollowResponse unfollow(String followerLoginId, String followeeLoginId) {
        if (followerLoginId.equals(followeeLoginId)) {
            throw new CustomException(ErrorCode.CANNOT_FOLLOW_OR_UNFOLLOW_SELF);
        }

        if (!isFollowExists(followerLoginId, followeeLoginId)) {
            throw new CustomException(ErrorCode.FOLLOW_NOT_FOUND);
        }

        followRepository.deleteByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);
        return FollowResponse.unfollow(followerLoginId, followeeLoginId);
    }

    @Transactional(readOnly = true)
    public Set<String> getFolloweeLoginIds(String loginId) {
        return followRepository.findAllWithFollowerAndFolloweeByFollower_LoginId(loginId).stream()
                .map(Follow::getFollowee)
                .map(User::getLoginId)
                .collect(Collectors.toSet());
    }

    private boolean isFollowExists(String followerLoginId, String followeeLoginId) {
        return followRepository.existsByFollower_LoginIdAndFollowee_LoginId(followerLoginId, followeeLoginId);
    }

    private User getFollowerOrException(String followerLoginId) {
        return userRepository.findByLoginId(followerLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private User getFolloweeOrException(String followeeLoginId) {
        return userRepository.findByLoginId(followeeLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOWEE_NOT_FOUND));
    }

    private static PageRequest makePageable(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
