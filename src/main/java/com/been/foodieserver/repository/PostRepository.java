package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {

    int countByUser_LoginId(String loginId);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Post> findAllByUser_LoginId(Pageable pageable, String loginId);

    @EntityGraph(attributePaths = {"user", "category"})
    Page<Post> findAllByUser_LoginIdIn(Pageable pageable, Set<String> loginIds);

    Optional<Post> findByIdAndUser_LoginId(Long postId, String userLoginId);

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Post> findWithFetchJoinById(Long postId);

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Post> findWithFetchJoinByIdAndUser_LoginId(Long postId, String userLoginId);
}
