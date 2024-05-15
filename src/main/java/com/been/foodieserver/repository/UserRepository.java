package com.been.foodieserver.repository;

import com.been.foodieserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndLoginIdIsNot(String nickname, String loginId);

    Optional<User> findByLoginId(String loginId);
}
