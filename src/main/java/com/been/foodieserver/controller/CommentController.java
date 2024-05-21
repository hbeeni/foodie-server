package com.been.foodieserver.controller;

import com.been.foodieserver.dto.request.CommentRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.CommentResponse;
import com.been.foodieserver.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/posts/{postId}/comments")
@RestController
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> writeComment(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @PathVariable("postId") @Positive(message = "postId는 0보다 커야 합니다.") Long postId,
                                                                     @RequestBody @Valid CommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commentService.writeComment(userDetails.getUsername(), postId, request.toDto())));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> modifyComment(@AuthenticationPrincipal UserDetails userDetails,
                                                                      @PathVariable("postId") @Positive(message = "postId는 0보다 커야 합니다.") Long postId,
                                                                      @PathVariable("commentId") @Positive(message = "commentId는 0보다 커야 합니다.") Long commentId,
                                                                      @RequestBody @Valid CommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commentService.modifyComment(userDetails.getUsername(), postId, commentId, request.toDto())));
    }
}
