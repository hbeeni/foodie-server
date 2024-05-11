package com.been.foodieserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public CustomException(ErrorCode errorCode) {
        status = errorCode.getStatus();
        message = errorCode.getMessage();
    }
}
