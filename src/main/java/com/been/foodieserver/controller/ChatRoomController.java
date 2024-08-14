package com.been.foodieserver.controller;

import com.been.foodieserver.dto.ChatRoomDto;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/chat-rooms")
@RestController
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getRoomList() {
        return ResponseEntity.ok(ApiResponse.success(chatRoomRepository.findAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomDto>> createRoom(@RequestParam("name") String name) {
        return ResponseEntity.ok(ApiResponse.success(chatRoomRepository.create(name)));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDto>> getRoom(@PathVariable("roomId") String roomId) {
        return ResponseEntity.ok(ApiResponse.success(chatRoomRepository.findById(roomId)));
    }
}
