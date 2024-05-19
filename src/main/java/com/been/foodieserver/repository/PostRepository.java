package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Post> findAll(Pageable pageable);

    Optional<Post> findByIdAndUser_LoginId(Long postId, String userLoginId);

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Post> findWithFetchJoinById(Long postId);

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Post> findWithFetchJoinByIdAndUser_LoginId(Long postId, String userLoginId);
}
