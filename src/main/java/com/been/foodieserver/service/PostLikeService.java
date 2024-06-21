package com.been.foodieserver.service;

import com.been.foodieserver.domain.Like;
import com.been.foodieserver.domain.NotificationType;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.NotificationEventDto;
import com.been.foodieserver.dto.response.LikeResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.producer.NotificationProducer;
import com.been.foodieserver.repository.LikeRepository;
import com.been.foodieserver.repository.cache.LikeCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class PostLikeService {

    private final UserService userService;
    private final PostService postService;
    private final LikeRepository likeRepository;
    private final LikeCacheRepository likeCacheRepository;
    private final NotificationProducer notificationProducer;

    public LikeResponse like(String loginId, Long postId) {
        if (hasUserLikedPost(loginId, postId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        Post post = postService.getPostWithFetchJoinOrException(postId);

        if (post.getUser().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.LIKE_OWN_POST);
        }

        User user = userService.getUserOrException(loginId);

        Like like = Like.of(user, post);
        likeRepository.save(like);
        likeCacheRepository.save(like); //redis save

        //event send
        notificationProducer.send(NotificationEventDto.of(post.getUser(),
                NotificationType.NEW_LIKE_ON_POST,
                user,
                post.getId()));

        return LikeResponse.of(loginId, postId);
    }

    public void unlike(String loginId, Long postId) {
        int resultCount = likeRepository.deleteByUserLoginIdAndPostId(loginId, postId);

        if (resultCount == 0) {
            throw new CustomException(ErrorCode.LIKE_NOT_FOUND);
        }

        likeCacheRepository.deleteByUserLoginIdAndPostId(loginId, postId); //redis delete
    }

    private boolean hasUserLikedPost(String loginId, Long postId) {
        return likeCacheRepository.existsByUserLoginIdAndPostId(loginId, postId);
    }
}
