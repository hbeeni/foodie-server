package com.been.foodieserver.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DUPLICATE_ID(HttpStatus.CONFLICT, "중복 ID 입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "중복 닉네임 입니다."),
    INVALID_PASSWORD(HttpStatus.FORBIDDEN, "비밀번호가 일치하지 않습니다"),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."),
    AUTH_FAIL(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    FOLLOW_NOT_FOUND(HttpStatus.BAD_REQUEST, "팔로우 정보를 찾을 수 없습니다."),
    FOLLOWEE_NOT_FOUND(HttpStatus.BAD_REQUEST, "팔로우할 사용자를 찾을 수 없습니다."),
    CANNOT_FOLLOW_OR_UNFOLLOW_SELF(HttpStatus.BAD_REQUEST, "본인을 팔로우/언팔로우할 수 없습니다."),

    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "카테고리를 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error");

    private final HttpStatus status;
    private final String message;
}
