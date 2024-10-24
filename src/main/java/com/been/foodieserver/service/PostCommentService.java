package com.been.foodieserver.service;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.NotificationType;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CommentDto;
import com.been.foodieserver.dto.NotificationEventDto;
import com.been.foodieserver.dto.response.CommentResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.producer.NotificationProducer;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.PostRepository;
import com.been.foodieserver.repository.cache.CommentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PostCommentService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentCacheRepository commentCacheRepository;
    private final NotificationProducer notificationProducer;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentList(Long postId, int pageNum, int pageSize) {
        validatePostExistsById(postId);

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        return commentRepository.findAllWithUserAndPostAndCategoryByPostId(pageable, postId).map(CommentResponse::of);
    }

    public CommentResponse writeComment(String loginId, Long postId, CommentDto dto) {
        Post post = postRepository.findWithUserAndCategoryById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userService.getUserOrException(loginId);

        Comment savedComment = commentRepository.save(dto.toEntity(post, user));
        commentCacheRepository.saveId(savedComment); //redis save

        //event send
        notificationProducer.send(NotificationEventDto.of(post.getUser(),
                NotificationType.NEW_COMMENT_ON_POST,
                user,
                post.getId()));

        return CommentResponse.of(savedComment);
    }

    public CommentResponse modifyComment(String loginId, Long postId, Long commentId, CommentDto dto) {
        validatePostExistsById(postId);

        Comment comment = commentRepository.findWithUserAndPostAndCategoryByIdAndLoginIdAndPostId(commentId, loginId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        comment.modify(dto.getContent());

        commentRepository.flush();

        return CommentResponse.of(comment);
    }

    public void deleteComment(String loginId, Long postId, Long commentId) {
        validatePostExistsById(postId);

        int resultCount = commentRepository.deleteByIdAndPostIdAndUserLoginId(commentId, postId, loginId);

        if (resultCount == 0) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        
        commentCacheRepository.deleteByPostIdAndId(postId, commentId); //redis delete
    }

    private void validatePostExistsById(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
    }
}
