package com.been.foodieserver.service;

import com.been.foodieserver.domain.Like;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.response.LikeResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.LikeRepository;
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

    public LikeResponse like(String loginId, Long postId) {
        if (hasUserLikedPost(loginId, postId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        Post post = postService.getPostWithFetchJoinOrException(postId);

        if (post.getUser().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.LIKE_OWN_POST);
        }

        User user = userService.getUserOrException(loginId);

        likeRepository.save(Like.of(user, post));

        return LikeResponse.of(loginId, postId);
    }

    private boolean hasUserLikedPost(String loginId, Long postId) {
        return likeRepository.existsByUser_LoginIdAndPost_Id(loginId, postId);
    }
}
