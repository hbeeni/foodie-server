package com.been.foodieserver.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum JwtErrorCode {

    NOT_FOUND_TOKEN(HttpStatus.UNAUTHORIZED, "JWT 토큰 형식의 값을 찾을 수 없습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 JWT 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, "해당 권한으로는 접근할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
