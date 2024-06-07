package com.been.foodieserver.consumer;

import com.been.foodieserver.dto.SlackEventDto;
import com.been.foodieserver.service.SlackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SlackConsumer {

    private final SlackService slackService;

    @KafkaListener(topics = "${spring.kafka.topic.slack}", groupId = "slack")
    public void consume(SlackEventDto eventDto, Acknowledgment ack) {
        log.info("[consume the event] {}", eventDto);
        slackService.sendSlackMessage(eventDto.getChannel(), eventDto.getMessage());
        ack.acknowledge();
    }
}
