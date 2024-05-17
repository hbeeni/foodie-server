package com.been.foodieserver.dto.request;

import com.been.foodieserver.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.been.foodieserver.dto.request.UserRequestValidation.LOGIN_ID_MESSAGE;
import static com.been.foodieserver.dto.request.UserRequestValidation.LOGIN_ID_PATTERN;
import static com.been.foodieserver.dto.request.UserRequestValidation.NICKNAME_MESSAGE;
import static com.been.foodieserver.dto.request.UserRequestValidation.NICKNAME_PATTERN;
import static com.been.foodieserver.dto.request.UserRequestValidation.PASSWORD_MESSAGE;
import static com.been.foodieserver.dto.request.UserRequestValidation.PASSWORD_PATTERN;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpRequest {

    @Pattern(regexp = LOGIN_ID_PATTERN, message = LOGIN_ID_MESSAGE)
    @Size(min = 5, max = 20)
    @NotBlank
    private String loginId;

    @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @Size(min = 8, max = 20)
    @NotBlank
    private String password;

    @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @Size(min = 8, max = 20)
    @NotBlank
    private String confirmPassword;

    @Pattern(regexp = NICKNAME_PATTERN, message = NICKNAME_MESSAGE)
    @Size(min = 2, max = 20)
    @NotBlank
    private String nickname;

    public UserDto toDto() {
        return UserDto.builder()
                .loginId(loginId)
                .password(password)
                .confirmPassword(confirmPassword)
                .nickname(nickname)
                .build();
    }
}
