package com.been.foodieserver.service;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Post;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        userRepository.findAll().forEach(userCacheRepository::save);
        log.info("set users to redis complete");
    }

    public void refreshPosts() {
        categoryRepository.findAll().forEach(categoryCacheRepository::save);

        for (int i = 0; i < 10; i++) {
            log.info("i={}", i);
            Pageable pageable = PageRequest.of(i, 10000, Sort.by(Sort.Direction.DESC, "id"));
            List<Post> posts = postRepository.findAllWithUserAndCategory(pageable).getContent();

            for (Post post : posts) {
                postCacheRepository.save(post);
            }
        }

        log.info("set posts to redis complete");
    }

    public void refreshComments() {
        for (int i = 0; i < 10; i++) {
            log.info("i={}", i);
            Pageable pageable = PageRequest.of(i, 10000, Sort.by(Sort.Direction.DESC, "id"));
            List<Comment> comments = commentRepository.findAllWithPost(pageable).getContent();

            for (Comment comment : comments) {
                commentCacheRepository.saveId(comment);
            }
        }

        log.info("set comments to redis complete");
    }
}
