package com.been.foodieserver.dto.request;

import com.been.foodieserver.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.been.foodieserver.dto.request.UserRequestValidation.NICKNAME_MESSAGE;
import static com.been.foodieserver.dto.request.UserRequestValidation.NICKNAME_PATTERN;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoModifyRequest {

    @Pattern(regexp = NICKNAME_PATTERN, message = NICKNAME_MESSAGE)
    @Size(min = 2, max = 20)
    @NotBlank
    private String nickname; //영문 소문자, 숫자만 가능 / 공백 불가 / 2 ~ 20자

    public UserDto toDto() {
        return UserDto.builder()
                .nickname(nickname)
                .build();
    }
}
