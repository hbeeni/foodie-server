package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c " +
            "join fetch c.user u " +
            "join fetch c.post p " +
            "join fetch p.category " +
            "where u.loginId = :loginId and p.id = :postId and c.id = :commentId")
    Optional<Comment> findWithUserAndPostAndCategoryByIdAndLoginIdAndPostId(@Param("commentId") Long commentId, @Param("loginId") String loginId, @Param("postId") Long postId);
}