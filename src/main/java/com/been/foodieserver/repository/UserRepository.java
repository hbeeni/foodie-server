package com.been.foodieserver.repository;

import com.been.foodieserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndLoginIdIsNot(String nickname, String loginId);

    Optional<User> findByLoginId(String loginId);

    @Query(nativeQuery = true, value = "select u.id from users u where u.deleted_at <= :deletedAt")
    List<Long> findAllByDeletedAtBefore(@Param("deletedAt") Timestamp deletedAt);

    @Modifying
    @Query(nativeQuery = true, value = "delete from users u where u.id in :userIds")
    int hardDeleteByIdIn(@Param("userIds") List<Long> userIds);
}
