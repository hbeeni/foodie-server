package com.been.foodieserver.controller;

import com.been.foodieserver.dto.ChatMessageDto;
import com.been.foodieserver.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class WebSocketController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;

    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message) {
        messagingTemplate.convertAndSend("/sub/chat-rooms/" + message.getRoomId(), message);
        chatRoomRepository.increaseMessageCount(message.getRoomId());
    }
}
