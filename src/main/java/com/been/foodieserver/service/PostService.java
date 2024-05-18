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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PostService {

    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    public PostResponse writePost(String loginId, PostDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        User user = userService.getUserEntityOrException(loginId);

        Post savedPost = postRepository.save(dto.toEntity(user, category));

        return PostResponse.of(user, category, savedPost);
    }
}
