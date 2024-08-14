package com.been.foodieserver.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@ToString
@NoArgsConstructor
@Getter
public class ChatRoomDto {

    private String roomId;
    private String name;
    private int messageCount;

    @Builder
    private ChatRoomDto(String name) {
        this.roomId = UUID.randomUUID().toString();
        this.name = name;
    }

    public static ChatRoomDto create(String name) {
        return ChatRoomDto.builder().name(name).build();
    }

    public void increaseMessageCount() {
        messageCount++;
    }
}
