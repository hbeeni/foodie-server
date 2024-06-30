package com.been.foodieserver.controller;

import com.been.foodieserver.service.RefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/refresh")
@RestController
public class RefreshController {

    private final RefreshService refreshService;

    @GetMapping("/test")
    public ResponseEntity<String> admin() {
        return ResponseEntity.ok("admin");
    }

    @GetMapping("/users")
    public ResponseEntity<Void> refreshUsers() {
        refreshService.refreshUsers();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts")
    public ResponseEntity<Void> refreshPosts() {
        refreshService.refreshPosts();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comments")
    public ResponseEntity<Void> refreshComments() {
        refreshService.refreshComments();
        return ResponseEntity.ok().build();
    }
}
