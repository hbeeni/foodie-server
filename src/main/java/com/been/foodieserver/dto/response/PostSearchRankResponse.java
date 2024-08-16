package com.been.foodieserver.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostSearchRankResponse {

    private String keyword;
    private Double score;

    public static PostSearchRankResponse of(String keyword, Double score) {
        return new PostSearchRankResponse(keyword, score);
    }
}
