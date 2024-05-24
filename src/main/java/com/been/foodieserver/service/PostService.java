package com.been.foodieserver.service;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Like;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.PostDto;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.CategoryRepository;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.LikeRepository;
import com.been.foodieserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PostService {

    private final UserService userService;
    private final FollowService followService;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public Page<PostResponse> getPostList(int pageNum, int pageSize) {
        Pageable pageable = makePageable(pageNum, pageSize);
        return postRepository.findAll(pageable).map(PostResponse::of);
    }

    public Page<PostResponse> getMyPostList(String loginId, int pageNum, int pageSize) {
        Pageable pageable = makePageable(pageNum, pageSize);
        return postRepository.findAllByUser_LoginId(pageable, loginId).map(PostResponse::of);
    }

    public Page<PostResponse> getPostListByUserLoginId(String writerLoginId, int pageNum, int pageSize) {
        if (!userService.isLoginIdExist(writerLoginId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        PageRequest pageable = makePageable(pageNum, pageSize);
        return postRepository.findAllByUser_LoginId(pageable, writerLoginId).map(PostResponse::of);
    }

    /**
     * 팔로우한 유저의 게시글 목록 조회
     */
    public Page<PostResponse> getPostsByFollowees(String loginId, int pageNum, int pageSize) {
        Pageable pageable = makePageable(pageNum, pageSize);
        Set<String> followeeLoginIdSet = followService.getFolloweeLoginIds(loginId);

        if (followeeLoginIdSet.isEmpty()) {
            return Page.empty(pageable);
        }

        return postRepository.findAllByUser_LoginIdIn(pageable, followeeLoginIdSet).map(PostResponse::of);
    }

    /**
     * 좋아요한 게시글 목록 조회
     */
    public Page<PostResponse> getLikedPostList(String loginId, int pageNum, int pageSize) {
        Pageable pageable = makePageable(pageNum, pageSize);

        List<Like> likes = likeRepository.findByUser_LoginId(loginId);

        if (likes.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> likedPostIds = likes.stream().map(Like::getPost).map(Post::getId).toList();
        return postRepository.findAllByIdIn(pageable, likedPostIds).map(PostResponse::of);
    }

    public PostResponse getPost(Long postId) {
        Post post = getPostWithFetchJoinOrException(postId);
        return PostResponse.of(post);
    }

    public PostResponse writePost(String loginId, PostDto dto) {
        Category category = getCategoryOrException(dto.getCategoryId());
        User user = userService.getUserOrException(loginId);

        Post savedPost = postRepository.save(dto.toEntity(user, category));

        return PostResponse.of(user, category, savedPost);
    }

    public PostResponse modifyPost(String loginId, Long postId, PostDto dto) {
        Category category = getCategoryOrException(dto.getCategoryId());
        Post post = getPostByUserOrException(postId, loginId);
        User user = userService.getUserOrException(loginId);

        post.modify(category, dto.getTitle(), dto.getContent());

        postRepository.flush();

        return PostResponse.of(user, category, post);
    }

    public PostResponse deletePost(String loginId, Long postId) {
        Post post = getPostWithFetchJoinByUserOrException(postId, loginId);
        post.delete();

        postRepository.flush();

        return PostResponse.of(post);
    }

    /**
     * 매일 3시 30분 삭제된 지 7일이 지난 게시글 삭제
     */
    @Scheduled(cron = "${schedules.cron.post.delete}")
    public void hardDeletePostsDeletedFor7Days() {
        log.info("hard delete posts");

        Timestamp sevenDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(7));
        List<Long> postIdsToDelete = postRepository.findAllByDeletedAtBefore(sevenDaysAgo);

        likeRepository.deleteByPostIdIn(postIdsToDelete);
        commentRepository.deleteByPostIdIn(postIdsToDelete);

        int deletedCount = postRepository.hardDeleteByPostIdIn(postIdsToDelete);

        if (postIdsToDelete.size() != deletedCount) {
            log.error("post deletion not successful. to be deleted: {}, deleted: {}", postIdsToDelete.size(), deletedCount);
        } else {
            log.info("delete {} posts", deletedCount);
        }
    }

    private static PageRequest makePageable(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
    }

    public Post getPostWithFetchJoinOrException(Long postId) {
        return postRepository.findWithFetchJoinById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Post getPostWithFetchJoinByUserOrException(Long postId, String loginId) {
        return postRepository.findWithFetchJoinByIdAndUser_LoginId(postId, loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Post getPostByUserOrException(Long postId, String loginId) {
        return postRepository.findByIdAndUser_LoginId(postId, loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Category getCategoryOrException(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
