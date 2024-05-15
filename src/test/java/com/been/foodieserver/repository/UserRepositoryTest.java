package com.been.foodieserver.repository;

import com.been.foodieserver.config.JpaConfig;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(JpaConfig.class)
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("찾는 닉네임이 다른 유저의 닉네임과 중복되면 true 반환")
    @Test
    void returnTrue_IfSearchedNicknameIsDuplicateWithOtherUsers() {
        //Given
        User user1 = User.of("user1", "pwd", "nick1", Role.USER);
        userRepository.saveAndFlush(user1);

        //When
        boolean result = userRepository.existsByNicknameAndLoginIdIsNot("nick1", "user2");

        //Then
        Assertions.assertThat(result).isTrue();
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
        Assertions.assertThat(result).isFalse();
    }
}
