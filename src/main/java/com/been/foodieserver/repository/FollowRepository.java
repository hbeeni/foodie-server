package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollower_LoginIdAndFollowee_LoginId(String followerLoginId, String followeeLoginId);

    void deleteByFollower_LoginIdAndFollowee_LoginId(String followerLoginId, String followeeLoginId);
}
