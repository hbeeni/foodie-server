package com.been.foodieserver.controller;

import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.service.ImageService;
import com.been.foodieserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/users/images")
@RestController
public class UserImageController {

    private final UserService userService;
    private final ImageService imageService;

    /**
     * imageName 파라미터를 입력하지 않으면 기본 프로필 이미지가 반환됩니다.
     */
    @GetMapping
    public ResponseEntity<Resource> getProfileImage(@RequestParam(value = "imageName", required = false) String imageName) {
        if (!StringUtils.hasText(imageName)) {
            imageName = "default/profile.jpg";
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/jpeg")).body(imageService.get(imageName));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("profileImage") MultipartFile file) {
        try {
            String savedImageName = imageService.save(file);
            userService.uploadProfileImage(userDetails.getUsername(), savedImageName);
            return ResponseEntity.ok(ApiResponse.success(savedImageName));
        } catch (IOException e) {
            return ResponseEntity.status(ErrorCode.IMAGE_UPLOAD_FAIL.getStatus())
                    .body(ApiResponse.error(ErrorCode.IMAGE_UPLOAD_FAIL.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteProfileImage(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
