package com.been.foodieserver.dto.request;

import com.been.foodieserver.dto.PostSearchDto;
import com.been.foodieserver.dto.PostSearchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchRequest {

    @NotNull
    private PostSearchType searchType;

    @NotBlank
    private String keyword;

    @Positive
    private Integer pageNum = 1;

    @Positive
    private Integer pageSize = 10;

    public PostSearchDto toDto() {
        return PostSearchDto.builder()
                .searchType(searchType)
                .keyword(keyword)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }
}
