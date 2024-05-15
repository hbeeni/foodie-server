package com.been.foodieserver.dto.request;

import com.been.foodieserver.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserModifyRequest {

    @Pattern(regexp = "^[a-z0-9]{2,20}$", message = "닉네임은 2 ~ 20자의 영문 소문자, 숫자만 사용 가능합니다.")
    @Size(min = 2, max = 20)
    @NotBlank
    private String nickname; //영문 소문자, 숫자만 가능 / 공백 불가 / 2 ~ 20자

    public UserDto toDto() {
        return UserDto.builder()
                .nickname(nickname)
                .build();
    }
}
