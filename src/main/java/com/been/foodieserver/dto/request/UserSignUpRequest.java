package com.been.foodieserver.dto.request;

import com.been.foodieserver.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSignUpRequest {

    @Pattern(regexp = "^(?=.*[a-z])[a-z0-9]{5,20}$", message = "아이디는 5 ~ 20자 사이여야 합니다. 영문 소문자는 필수, 숫자는 옵션입니다.")
    @Size(min = 5, max = 20)
    @NotBlank
    private String loginId; //영문 소문자 필수 / 숫자 옵션 / 공백 불가 / 5 ~ 20자

    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[A-Z]?)[a-zA-Z0-9]{8,20}$", message = "비밀번호는 8 ~ 20자 사이여야 합니다. 영문 소문자, 숫자를 포함해야 하며, 영문 대문자는 옵션입니다.")
    @Size(min = 8, max = 20)
    @NotBlank
    private String password; //영문 소문자, 숫자 포함 필수 / 영문 대문자 옵션 / 공백 불가 / 8 ~ 20자

    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[A-Z]?)[a-zA-Z0-9]{8,20}$", message = "비밀번호는 8 ~ 20자 사이여야 합니다. 영문 소문자, 숫자를 포함해야 하며, 영문 대문자는 옵션입니다.")
    @Size(min = 8, max = 20)
    @NotBlank
    private String confirmPassword;

    @Pattern(regexp = "^[a-z0-9]{2,20}$", message = "닉네임은 2 ~ 20자의 영문 소문자, 숫자만 사용 가능합니다.")
    @Size(min = 2, max = 20)
    @NotBlank
    private String nickname; //영문 소문자, 숫자만 가능 / 공백 불가 / 2 ~ 20자

    public UserDto toDto() {
        return UserDto.of(loginId, password, confirmPassword, nickname);
    }
}
