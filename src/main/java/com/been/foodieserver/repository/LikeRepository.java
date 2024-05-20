package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUser_LoginIdAndPost_Id(String userLoginId, Long postId);
}
