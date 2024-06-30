package com.been.foodieserver.service;

import com.been.foodieserver.domain.User;
import com.been.foodieserver.repository.CategoryRepository;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.PostRepository;
import com.been.foodieserver.repository.UserRepository;
import com.been.foodieserver.repository.cache.CategoryCacheRepository;
import com.been.foodieserver.repository.cache.CommentCacheRepository;
import com.been.foodieserver.repository.cache.PostCacheRepository;
import com.been.foodieserver.repository.cache.UserCacheRepository;
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
public class RefreshService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CategoryCacheRepository categoryCacheRepository;
    private final PostCacheRepository postCacheRepository;
    private final UserCacheRepository userCacheRepository;
    private final CommentCacheRepository commentCacheRepository;

    public void refreshUsers() {
        boolean hasNext = true;
        int i = 0;

        while (hasNext) {
            log.info("i={}", i);
            Pageable pageable = PageRequest.of(i, 10000, Sort.by(Sort.Direction.DESC, "id"));
            Page<User> result = userRepository.findAll(pageable);

            result.getContent().forEach(userCacheRepository::save);
            hasNext = result.hasNext();
            i++;
        }

        log.info("set users to redis complete");
    }

    public void refreshPosts() {
        categoryRepository.findAll().forEach(categoryCacheRepository::save);
        log.info("set posts to redis start");
        postRepository.findAllWithUserAndCategory().forEach(postCacheRepository::save);
        log.info("set posts to redis complete");
    }

    public void refreshComments() {
        log.info("set comments to redis start");
        commentRepository.findAllWithPost().forEach(commentCacheRepository::saveId);
        log.info("set comments to redis complete");
    }
}
