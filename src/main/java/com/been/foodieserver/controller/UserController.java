package com.been.foodieserver.controller;

import com.been.foodieserver.dto.request.UserModifyRequest;
import com.been.foodieserver.dto.request.UserSignUpRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.UserInfoResponse;
import com.been.foodieserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/users")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody @Valid UserSignUpRequest request) {
        userService.signUp(request.toDto());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/id/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkLoginId(@RequestParam String loginId) {
        return ResponseEntity.ok(ApiResponse.success(userService.isLoginIdDuplicated(loginId)));
    }

    @GetMapping("/nickname/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(ApiResponse.success(userService.isNicknameDuplicated(nickname)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyInfo(userDetails.getUsername())));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserInfo(userId)));
    }

    @PutMapping("/my")
    public ResponseEntity<ApiResponse<UserInfoResponse>> modifyMyInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserModifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.modifyMyInfo(userDetails.getUsername(), request.toDto())));
    }
}
