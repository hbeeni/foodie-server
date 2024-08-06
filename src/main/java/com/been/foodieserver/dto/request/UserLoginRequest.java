package com.been.foodieserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.been.foodieserver.dto.request.UserRequestValidation.LOGIN_ID_MESSAGE;
import static com.been.foodieserver.dto.request.UserRequestValidation.LOGIN_ID_PATTERN;
import static com.been.foodieserver.dto.request.UserRequestValidation.PASSWORD_MESSAGE;
import static com.been.foodieserver.dto.request.UserRequestValidation.PASSWORD_PATTERN;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {

    @Pattern(regexp = LOGIN_ID_PATTERN, message = LOGIN_ID_MESSAGE)
    @Size(min = 5, max = 20)
    @NotBlank
    private String loginId;

    @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @Size(min = 8, max = 20)
    @NotBlank
    private String password;
}
