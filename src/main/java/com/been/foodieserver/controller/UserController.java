package com.been.foodieserver.controller;

import com.been.foodieserver.dto.request.UserInfoModifyRequest;
import com.been.foodieserver.dto.request.UserLoginRequest;
import com.been.foodieserver.dto.request.UserPasswordChangeRequest;
import com.been.foodieserver.dto.request.UserSignUpRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.UserInfoResponse;
import com.been.foodieserver.dto.response.UserInfoWithStatisticsResponse;
import com.been.foodieserver.service.UserService;
import com.been.foodieserver.utils.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/users")
@RestController
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody @Valid UserSignUpRequest request) {
        userService.signUp(request.toDto());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> loginUser(@RequestBody @Valid UserLoginRequest request, HttpServletResponse response) {
        String jwt = createJwtToken(request.getLoginId(), request.getPassword(), response);
        return ResponseEntity.ok(ApiResponse.success(Map.of("token", jwt)));
    }

    @GetMapping("/id/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkLoginId(@RequestParam("loginId") String loginId) {
        return ResponseEntity.ok(ApiResponse.success(userService.isLoginIdExist(loginId)));
    }

    @GetMapping("/nickname/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(ApiResponse.success(userService.isNicknameExist(nickname)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<UserInfoWithStatisticsResponse>> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyInfo(userDetails.getUsername())));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoWithStatisticsResponse>> getUserInfo(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserInfo(userId)));
    }

    @PutMapping("/my")
    public ResponseEntity<ApiResponse<UserInfoResponse>> modifyMyInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserInfoModifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.modifyMyInfo(userDetails.getUsername(), request.toDto())));
    }

    @PutMapping("/my/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserPasswordChangeRequest request) {
        userService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword(), request.getConfirmNewPassword());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/my")
    public ResponseEntity<ApiResponse<UserInfoResponse>> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserInfoResponse response = userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String createJwtToken(String loginId, String password, HttpServletResponse response) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginId, password);
        Authentication authentication = authenticationManager.authenticate(authenticationRequest);
        String jwt = jwtTokenProvider.createToken(authentication);
        response.addHeader("Authorization", "Bearer " + jwt);

        return jwt;
    }
}
