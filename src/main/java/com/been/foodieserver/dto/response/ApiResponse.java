package com.been.foodieserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;
    private Pagination pagination;

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    public static final String STATUS_ERROR = "error";

    private ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    private ApiResponse(String status, String message, T data, Pagination pagination) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.pagination = pagination;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(STATUS_SUCCESS, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(STATUS_SUCCESS, null, data);
    }

    public static <T> ApiResponse<List<T>> success(Page<T> page) {
        return new ApiResponse<>(STATUS_SUCCESS, null, page.getContent(), Pagination.of(page));
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

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pagination {

        private int currentPage; //현재 페이지
        private int totalPages; //전체 페이지 수
        private long totalElements; //전체 데이터 수
        private int pageSize; //한 페이지 당 데이터 개수
        private Boolean hasPrevious;
        private Boolean hasNext;

        public static Pagination of(Page<?> page) {
            return new Pagination(page.getNumber() + 1, page.getTotalPages(), page.getTotalElements(), page.getSize(), page.hasPrevious(), page.hasNext());
        }
    }
}
