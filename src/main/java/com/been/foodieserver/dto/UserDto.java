package com.been.foodieserver.dto;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UserDto {

    private final Long id;
    private final String loginId;
    private final String password;
    private final String confirmPassword;
    private final String nickname;

    @Builder
    private UserDto(Long id, String loginId, String password, String confirmPassword, String nickname) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.nickname = nickname;
    }

    public User toEntity(String encodedPassword) {
        return User.of(loginId, encodedPassword, nickname, Role.USER);
    }
}
