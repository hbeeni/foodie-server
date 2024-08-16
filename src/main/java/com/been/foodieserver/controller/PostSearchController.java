package com.been.foodieserver.controller;

import com.been.foodieserver.dto.request.PostSearchRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.dto.response.PostSearchRankResponse;
import com.been.foodieserver.service.PostSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/posts/search")
@RestController
public class PostSearchController {

    private final PostSearchService postSearchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> searchPost(@RequestBody @Valid PostSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(postSearchService.search(request.toDto())));
    }

    /**
     * 게시글 제목 기준 인기 검색어 10개를 반환합니다.
     */
    @GetMapping("/rank")
    public ResponseEntity<ApiResponse<List<PostSearchRankResponse>>> searchPostRankList() {
        return ResponseEntity.ok(ApiResponse.success(postSearchService.getTop10SearchKeywords()));
    }
}
