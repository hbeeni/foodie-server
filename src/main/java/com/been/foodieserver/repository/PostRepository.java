package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndUser_LoginId(Long postId, String userLoginId);
}
