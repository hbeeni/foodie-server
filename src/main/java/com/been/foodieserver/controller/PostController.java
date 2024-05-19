package com.been.foodieserver.controller;

import com.been.foodieserver.dto.request.PostWriteRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/posts")
@RestController
public class PostController {

    private final PostService postService;

    /**
     * @param pageNum  페이지 번호 (1 시작)
     * @param pageSize 페이지 당 게시글 수
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPostList(@RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
                                                                       @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostList(pageNum, pageSize)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPostList(@AuthenticationPrincipal UserDetails userDetails,
                                                                         @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
                                                                         @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(postService.getMyPostList(userDetails.getUsername(), pageNum, pageSize)));
    }

    @GetMapping("/users/{loginId}")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPostListByUser(@PathVariable("loginId") String writerLoginId,
                                                                             @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
                                                                             @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostListByUserLoginId(writerLoginId, pageNum, pageSize)));
    }

    @GetMapping("/follows")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getFeed(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
                                                                   @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostsByFollowees(userDetails.getUsername(), pageNum, pageSize)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPost(postId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> writePost(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid PostWriteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(postService.writePost(userDetails.getUsername(), request.toDto())));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> modifyPost(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("postId") Long postId, @RequestBody @Valid PostWriteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(postService.modifyPost(userDetails.getUsername(), postId, request.toDto())));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> deletePost(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("postId") Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.deletePost(userDetails.getUsername(), postId)));
    }
}
