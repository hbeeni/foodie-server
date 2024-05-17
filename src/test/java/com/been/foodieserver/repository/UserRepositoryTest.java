package com.been.foodieserver.repository;

import com.been.foodieserver.config.JpaConfig;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaConfig.class)
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @DisplayName("찾는 닉네임이 다른 유저의 닉네임과 중복되면 true 반환")
    @Test
    void returnTrue_IfSearchedNicknameIsDuplicateWithOtherUsers() {
        //Given
        User user1 = User.of("user1", "pwd", "nick1", Role.USER);
        userRepository.saveAndFlush(user1);

        //When
        boolean result = userRepository.existsByNicknameAndLoginIdIsNot("nick1", "user2");

        //Then
        assertThat(result).isTrue();
    }

    @DisplayName("찾는 닉네임이 다른 유저의 닉네임과 중복되지 않으면 false 반환")
    @Test
    void returnFalse_IfSearchedNicknameIsNotDuplicateWithOtherUsers() {
        //Given
        User user1 = User.of("user1", "pwd", "nick1", Role.USER);
        userRepository.saveAndFlush(user1);

        //When
        boolean result = userRepository.existsByNicknameAndLoginIdIsNot("nick2", "user2");

        //Then
        assertThat(result).isFalse();
    }

    @DisplayName("soft delete된 지 2일이 지난 User 삭제")
    @Test
    void deleteSoftDeletedUsersAfter2Days() {
        //Given
        User user1 = User.of("user1", "pwd", "nick1", Role.USER);
        User user2 = User.of("user2", "pwd", "nick2", Role.USER);
        User user3 = User.of("user3", "pwd", "nick3", Role.USER);

        ReflectionTestUtils.setField(user1, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        ReflectionTestUtils.setField(user2, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(2)));
        ReflectionTestUtils.setField(user3, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(3)));

        userRepository.saveAll(List.of(user1, user2, user3));
        em.flush();
        em.clear();

        //When
        int deletedCount = userRepository.deleteAllByDeletedAtBefore(Timestamp.valueOf(LocalDateTime.now().minusDays(2)));
        List result = em.createNativeQuery("select * from users u", User.class).getResultList();

        //Then
        assertThat(deletedCount).isEqualTo(2);
        assertThat(result).hasSize(1);
    }
}
