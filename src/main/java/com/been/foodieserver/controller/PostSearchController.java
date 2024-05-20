package com.been.foodieserver.controller;

import com.been.foodieserver.dto.request.PostSearchRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.PostResponse;
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
@RequestMapping("${api.endpoint.base-url}/posts")
@RestController
public class PostSearchController {

    private final PostSearchService postSearchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PostResponse>>> searchPost(@RequestBody @Valid PostSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(postSearchService.search(request.toDto())));
    }
}
