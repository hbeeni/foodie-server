package com.been.foodieserver.exception.handler;


import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    /**
     * RequestParam으로  설정한 파라미터가 입력되지 않았을 경우 발생하는 예외를 처리한다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> missingParameter(MissingServletRequestParameterException ex) {
        printLog(ex);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex));
    }

    /**
     * RequestBody로 설정한 객체의 유효성 검증이 실패할 때 발생하는 예외를 처리한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> validationFail(MethodArgumentNotValidException ex) {
        printLog(ex);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getBindingResult()));
    }

    /**
     * 비즈니스 예외를 처리한다.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        printLog(ex);
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.fail(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        printLog(ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    private static void printLog(Exception ex) {
        log.error("Error occurs! {}", ex.getMessage());
    }
}
