package com.been.foodieserver.service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class SlackService {

    @Value("${slack.token}")
    private String token;

    public void sendSlackMessage(SlackChannel channel, String message) {
        try {
            MethodsClient methodsClient = Slack.getInstance().methods(token);

            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channel.getChannelName())
                    .text(message)
                    .build();

            methodsClient.chatPostMessage(request);
            log.info("[Slack {}] {}", channel, message);
        } catch (SlackApiException | IOException e) {
            log.error(e.getMessage());
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum SlackChannel {
        AUTH("#authentication-logs"),
        ERROR("#error-logs");

        private final String channelName;
    }
}
