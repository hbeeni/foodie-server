package com.been.foodieserver.dto;

import com.been.foodieserver.service.SlackService.SlackChannel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SlackEventDto {

    private SlackChannel channel;
    private String message;

    public static SlackEventDto of(SlackChannel channel, String message) {
        return new SlackEventDto(channel, message);
    }
}
