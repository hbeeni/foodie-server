package com.been.foodieserver.service;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.PostDto;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.CategoryRepository;
import com.been.foodieserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return postRepository.findAllByUser_LoginIdIn(pageable, followeeLoginIdSet).map(PostResponse::of);
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findWithFetchJoinById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
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

    private static PageRequest makePageable(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
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
