package com.been.foodieserver.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PostSearchDto {

    private final PostSearchType searchType;
    private final String keyword;
    private final Integer pageNum;
    private final Integer pageSize;

    @Builder
    private PostSearchDto(PostSearchType searchType, String keyword, Integer pageNum, Integer pageSize) {
        this.searchType = searchType;
        this.keyword = keyword;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
