package com.been.foodieserver.dto.request;

import com.been.foodieserver.dto.PostSearchDto;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchRequest {

    private String writerLoginId;

    private String title;

    @Positive
    private Integer pageNum = 1;

    @Positive
    private Integer pageSize = 10;

    public PostSearchDto toDto() {
        return PostSearchDto.builder()
                .writerLoginId(writerLoginId)
                .title(title)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }
}
