package com.been.foodieserver.controller;

import com.been.foodieserver.service.RefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RefreshController {

    private final RefreshService refreshService;

    @GetMapping("/refresh/test")
    public ResponseEntity<String> admin() {
        return ResponseEntity.ok("admin");
    }

    @GetMapping("/refresh/users")
    public ResponseEntity<Void> refreshUsers() {
        refreshService.refreshUsers();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/refresh/posts")
    public ResponseEntity<Void> refreshPosts() {
        refreshService.refreshPosts();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/refresh/comments")
    public ResponseEntity<Void> refreshComments() {
        refreshService.refreshComments();
        return ResponseEntity.ok().build();
    }
}
