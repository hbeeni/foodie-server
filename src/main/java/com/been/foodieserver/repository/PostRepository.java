package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
