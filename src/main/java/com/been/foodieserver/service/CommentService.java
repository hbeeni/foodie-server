package com.been.foodieserver.service;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CommentDto;
import com.been.foodieserver.dto.response.CommentResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CommentService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentResponse writeComment(String loginId, Long postId, CommentDto dto) {
        Post post = postRepository.findWithFetchJoinById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userService.getUserOrException(loginId);

        Comment savedComment = commentRepository.save(dto.toEntity(post, user));

        return CommentResponse.of(savedComment);
    }
}
