package com.been.foodieserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    public static final String STATUS_ERROR = "error";

    private String status;
    private String message;
    private T data;
    private Pagination pagination;

    private ApiResponse(String status, String message, T data) {
        this(status, message, data, null);
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

    public static <T> ApiResponse<T> fail(String message, T data) {
        return new ApiResponse<>(STATUS_FAIL, message, data);
    }

    public static ApiResponse<Void> fail(Exception ex) {
        return new ApiResponse<>(STATUS_FAIL, ex.getLocalizedMessage(), null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(STATUS_FAIL, message, null);
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
