package com.been.foodieserver.repository;

import com.been.foodieserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);
}
