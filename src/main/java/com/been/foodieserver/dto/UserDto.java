package com.been.foodieserver.dto;

import com.been.foodieserver.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDto {

    private Long id;
    private String loginId;
    private String password;
    private String confirmPassword;
    private String nickname;

    public static UserDto of(String loginId, String password, String confirmPassword, String nickname) {
        return UserDto.of(null, loginId, password, confirmPassword, nickname);
    }

    public static UserDto of(Long id, String loginId, String password, String confirmPassword, String nickname) {
        return new UserDto(id, loginId, password, confirmPassword, nickname);
    }

    public User toEntity(String encodedPassword) {
        return User.builder()
                .loginId(loginId)
                .password(encodedPassword)
                .nickname(nickname)
                .build();
    }
}
