package com.been.foodieserver.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DUPLICATE_ID(HttpStatus.CONFLICT, "중복 ID 입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "중복 닉네임 입니다."),
    CHECK_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error");

    private final HttpStatus status;
    private final String message;
}
