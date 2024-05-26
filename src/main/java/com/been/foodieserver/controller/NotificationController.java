package com.been.foodieserver.controller;

import com.been.foodieserver.dto.CustomUserDetails;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.NotificationResponse;
import com.been.foodieserver.service.NotificationService;
import com.been.foodieserver.service.SseService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/notifications")
@RestController
public class NotificationController {

    private final NotificationService notificationService;
    private final SseService sseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationList(@AuthenticationPrincipal UserDetails userDetails,
                                                                                       @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
                                                                                       @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotificationList(userDetails.getUsername(), pageNum, pageSize)));
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(sseService.subscribe(customUserDetails.getId()));
    }
}
