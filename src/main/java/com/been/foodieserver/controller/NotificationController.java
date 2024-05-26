package com.been.foodieserver.controller;

import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.NotificationResponse;
import com.been.foodieserver.service.NotificationService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/notifications")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationList(@AuthenticationPrincipal UserDetails userDetails,
                                                                                       @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
                                                                                       @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotificationList(userDetails.getUsername(), pageNum, pageSize)));
    }
}
