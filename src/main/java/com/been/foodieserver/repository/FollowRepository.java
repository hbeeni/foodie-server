package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Follow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * 팔로우한 유저 조회
     */
    @EntityGraph(attributePaths = {"follower", "followee"})
    List<Follow> findAllByFollower_LoginId(String loginId);

    boolean existsByFollower_LoginIdAndFollowee_LoginId(String followerLoginId, String followeeLoginId);

    void deleteByFollower_LoginIdAndFollowee_LoginId(String followerLoginId, String followeeLoginId);
}
