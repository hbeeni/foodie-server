package com.been.foodieserver.controller;

import com.been.foodieserver.dto.request.UserInfoModifyRequest;
import com.been.foodieserver.dto.request.UserPasswordChangeRequest;
import com.been.foodieserver.dto.request.UserSignUpRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.UserInfoResponse;
import com.been.foodieserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody @Valid UserSignUpRequest request) {
        userService.signUp(request.toDto());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/id/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkLoginId(@RequestParam("loginId") String loginId) {
        return ResponseEntity.ok(ApiResponse.success(userService.isLoginIdDuplicated(loginId)));
    }

    @GetMapping("/nickname/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(ApiResponse.success(userService.isNicknameDuplicated(nickname)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyInfo(userDetails.getUsername())));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserInfo(userId)));
    }

    @PutMapping("/my")
    public ResponseEntity<ApiResponse<UserInfoResponse>> modifyMyInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserInfoModifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.modifyMyInfo(userDetails.getUsername(), request.toDto())));
    }

    @PutMapping("/my/password")
    public ResponseEntity<ApiResponse<UserInfoResponse>> changePassword(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
                                                                        @AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserPasswordChangeRequest request) {
        userService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword(), request.getConfirmNewPassword());
        forceLogout(servletRequest, servletResponse);
        return ResponseEntity.ok(ApiResponse.success());
    }

    private void forceLogout(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        logoutHandler.logout(servletRequest, servletResponse, SecurityContextHolder.getContext().getAuthentication());
    }
}
