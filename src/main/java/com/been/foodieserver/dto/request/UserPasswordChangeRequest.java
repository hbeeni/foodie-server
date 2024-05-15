package com.been.foodieserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.been.foodieserver.dto.request.UserRequestValidation.PASSWORD_MESSAGE;
import static com.been.foodieserver.dto.request.UserRequestValidation.PASSWORD_PATTERN;

@Getter
@AllArgsConstructor
public class UserPasswordChangeRequest {

    @NotBlank
    private String currentPassword;

    @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @Size(min = 8, max = 20)
    @NotBlank
    private String newPassword;

    @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
    @Size(min = 8, max = 20)
    @NotBlank
    private String confirmNewPassword;
}
