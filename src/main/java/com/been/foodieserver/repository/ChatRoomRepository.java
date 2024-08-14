package com.been.foodieserver.repository;

import com.been.foodieserver.dto.ChatRoomDto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ChatRoomRepository {

    private Map<String, ChatRoomDto> chatRoomMap = new LinkedHashMap<>();

    public List<ChatRoomDto> findAll() {
        ArrayList<ChatRoomDto> chatRooms = new ArrayList<>(chatRoomMap.values());
        Collections.reverse(chatRooms);
        return chatRooms;
    }

    public ChatRoomDto findById(String id) {
        return chatRoomMap.get(id);
    }

    public ChatRoomDto create(String name) {
        ChatRoomDto chatRoom = ChatRoomDto.create(name);
        chatRoomMap.put(chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    public void increaseMessageCount(String id) {
        ChatRoomDto chatRoom = findById(id);
        chatRoom.increaseMessageCount();
    }
}
