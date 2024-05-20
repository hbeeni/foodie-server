package com.been.foodieserver.controller;

import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.LikeResponse;
import com.been.foodieserver.service.PostLikeService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/posts")
@RestController
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> like(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("postId") @Min(1) Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postLikeService.like(userDetails.getUsername(), postId)));
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> unlike(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("postId") @Min(1) Long postId) {
        postLikeService.unlike(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
