package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
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

    @Query(nativeQuery = true, value = "select p.id from posts p where p.user_id in :userIds")
    List<Long> findAllByUserIdIn(@Param("userIds") List<Long> userIds);

    @Query(nativeQuery = true, value = "select p.id from posts p where p.deleted_at <= :deletedAt")
    List<Long> findAllByDeletedAtBefore(@Param("deletedAt") Timestamp deletedAt);

    Optional<Post> findByIdAndUser_LoginId(Long postId, String userLoginId);

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Post> findWithFetchJoinById(Long postId);

    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Post> findWithFetchJoinByIdAndUser_LoginId(Long postId, String userLoginId);

    @Modifying
    @Query(nativeQuery = true, value = "delete from posts p where p.id in :postIds")
    int hardDeleteByPostIdIn(@Param("postIds") List<Long> postIds);
}
