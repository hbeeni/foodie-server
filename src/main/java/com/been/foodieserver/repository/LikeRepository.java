package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUser_LoginIdAndPost_Id(String userLoginId, Long postId);

    @Modifying
    @Query("delete from Like l where l.user.loginId = :loginId and l.post.id = :postId")
    int deleteByUserLoginIdAndPostId(@Param("loginId") String userLoginId, @Param("postId") Long postId);

    @Modifying
    @Query("delete from Like l where l.user.id in :userIds")
    void deleteByUserIdIn(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("delete from Like l where l.post.id in :postIds")
    void deleteByPostIdIn(@Param("postIds") List<Long> postIds);
}
