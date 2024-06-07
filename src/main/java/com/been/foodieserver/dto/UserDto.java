package com.been.foodieserver.dto;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String loginId;
    private String password;
    private String confirmPassword;
    private String nickname;
    private String profileImage;
    private Role role;

    @Builder
    private UserDto(Long id, String loginId, String password, String confirmPassword, String nickname, String profileImage, Role role) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = role;
    }

    public User toEntity(String encodedPassword) {
        return User.of(loginId, encodedPassword, nickname, profileImage, Role.USER);
    }

    public User toEntity() {
        User user = User.of(loginId, password, nickname, profileImage, role);
        user.setId(id);
        return user;
    }
}
