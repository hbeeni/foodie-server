package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Follow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * 팔로우한 유저 조회
     */
    @EntityGraph(attributePaths = {"follower", "followee"})
    List<Follow> findAllWithFollowerAndFolloweeByFollower_LoginId(String loginId);

    /**
     * 유저가 팔로우한 사람 수 조회
     */
    int countByFollower_LoginId(String loginId);

    /**
     * 유저를 팔로우한 사람 수 조회
     */
    int countByFollowee_LoginId(String loginId);

    boolean existsByFollower_LoginIdAndFollowee_LoginId(String followerLoginId, String followeeLoginId);

    void deleteByFollower_LoginIdAndFollowee_LoginId(String followerLoginId, String followeeLoginId);

    @Modifying
    @Query("delete from Follow f where f.follower.id in :userIds")
    void deleteByFollowerIdIn(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("delete from Follow f where f.followee.id in :userIds")
    void deleteByFolloweeIdIn(@Param("userIds") List<Long> userIds);
}
