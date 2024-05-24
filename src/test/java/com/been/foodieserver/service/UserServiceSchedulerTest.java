package com.been.foodieserver.service;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(properties = {"schedules.cron.user.delete=0/2 * * * * *"})
class UserServiceSchedulerTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user1 = User.of("user1", "pwd", "nick1", Role.USER);
        User user2 = User.of("user2", "pwd", "nick2", Role.USER);
        User user3 = User.of("user3", "pwd", "nick3", Role.USER);
        User user4 = User.of("user4", "pwd", "nick4", Role.USER);
        User user5 = User.of("user5", "pwd", "nick5", Role.USER);

        ReflectionTestUtils.setField(user1, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(29)));
        ReflectionTestUtils.setField(user2, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(30)));
        ReflectionTestUtils.setField(user3, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(31)));
        ReflectionTestUtils.setField(user4, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(32)));

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
    }

    @DisplayName("30일 전에 탈퇴한 회원을 DB에서 삭제")
    @Test
    void schedule_DeleteUsersWithdrawn30DaysAgoFromDB() {
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    long userCount = (long) em.createNativeQuery("select count(u.id) from users u").getSingleResult();
                    assertThat(userCount).isEqualTo(2L);
                });
    }
}
