package com.been.foodieserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.HashMap;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    public static final String STATUS_ERROR = "error";

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(STATUS_SUCCESS, null, data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(STATUS_FAIL, message, null);
    }

    public static ApiResponse<Object> fail(Exception ex) {
        Map<String, String> errorMap = new HashMap<>();

        if (ex instanceof MissingServletRequestParameterException missingServletRequestParameterException) {
            String parameterName = missingServletRequestParameterException.getParameterName();
            errorMap.put(parameterName, "필수입니다.");

            return new ApiResponse<>(STATUS_FAIL, "request parameter errors", errorMap);
        }

        if (ex.getCause() != null) {
            Throwable cause = ex.getCause();
            return new ApiResponse<>(STATUS_FAIL, cause.getMessage(), null);
        }

        return new ApiResponse<>(STATUS_FAIL, ex.getMessage(), null);
    }

    public static ApiResponse<Map<String, String>> fail(BindingResult bindingResult) {
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors()
                .forEach(b -> errorMap.put(b.getField(), b.getDefaultMessage()));
        return new ApiResponse<>(STATUS_FAIL, "field errors", errorMap);
    }
}
