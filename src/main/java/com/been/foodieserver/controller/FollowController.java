package com.been.foodieserver.controller;

import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.FollowResponse;
import com.been.foodieserver.service.FollowService;
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
@RequestMapping("${api.endpoint.base-url}/follows")
@RestController
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followeeLoginId}")
    public ResponseEntity<ApiResponse<FollowResponse>> follow(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("followeeLoginId") String followeeLoginId) {
        return ResponseEntity.ok(ApiResponse.success(followService.follow(userDetails.getUsername(), followeeLoginId)));
    }

    @DeleteMapping("/{followeeLoginId}")
    public ResponseEntity<ApiResponse<FollowResponse>> unfollow(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("followeeLoginId") String followeeLoginId) {
        return ResponseEntity.ok(ApiResponse.success(followService.unfollow(userDetails.getUsername(), followeeLoginId)));
    }
}
