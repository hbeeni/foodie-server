package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c join fetch c.post")
    List<Comment> findAllWithPost();

    @Query(value = "select c from Comment c " +
            "join fetch c.user u " +
            "join fetch c.post p " +
            "join fetch p.category " +
            "where p.id = :postId",
            countQuery = "select count(c) from Comment c " +
                    "where c.post.id = :postId")
    Page<Comment> findAllWithUserAndPostAndCategoryByPostId(Pageable pageable, @Param("postId") Long postId);

    @Query("select c from Comment c " +
            "join fetch c.user u " +
            "join fetch c.post p " +
            "join fetch p.category " +
            "where u.loginId = :loginId and p.id = :postId and c.id = :commentId")
    Optional<Comment> findWithUserAndPostAndCategoryByIdAndLoginIdAndPostId(@Param("commentId") Long commentId, @Param("loginId") String loginId, @Param("postId") Long postId);

    @Modifying
    @Query("delete from Comment c " +
            "where c.id = :commentId and c.post.id = :postId and c.user.loginId = :loginId")
    int deleteByIdAndPostIdAndUserLoginId(@Param("commentId") Long commentId, @Param("postId") Long postId, @Param("loginId") String loginId);

    @Modifying
    @Query("delete from Comment c where c.user.id in :userIds")
    void deleteByUserIdIn(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("delete from Comment c where c.post.id in :postIds")
    void deleteByPostIdIn(@Param("postIds") List<Long> postIds);
}
