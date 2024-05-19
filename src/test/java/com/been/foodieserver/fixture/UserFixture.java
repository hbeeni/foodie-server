package com.been.foodieserver.fixture;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static User get(Long userId, String loginId) {
        User user = User.of(loginId, "pwd", loginId, Role.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
