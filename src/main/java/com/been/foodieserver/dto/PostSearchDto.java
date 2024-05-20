package com.been.foodieserver.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PostSearchDto {

    private final String writerLoginId;
    private final String title;
    private final Integer pageNum;
    private final Integer pageSize;

    @Builder
    private PostSearchDto(String writerLoginId, String title, Integer pageNum, Integer pageSize) {
        this.writerLoginId = writerLoginId;
        this.title = title;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
