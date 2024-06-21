package com.been.foodieserver.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageDto<T> {

    private int currentPage; //현재 페이지
    private int totalPages; //전체 페이지 수
    private long totalElements; //전체 데이터 수
    private int pageSize; //한 페이지 당 데이터 개수
    private Boolean hasPrevious;
    private Boolean hasNext;
    private List<T> content;

    private PageDto(int pageNum, int pageSize, long totalElements, List<T> content) {
        this.currentPage = pageNum;
        this.totalPages = pageSize == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) pageSize);
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.hasPrevious = pageNum != 1;
        this.hasNext = pageNum < totalPages;
        this.content = content;
    }

    public static <T> PageDto<T> of(int pageNum, int pageSize, long totalElements, List<T> content) {
        return new PageDto<>(pageNum, pageSize, totalElements, content);
    }
}
