package com.been.foodieserver.producer;

import com.been.foodieserver.dto.SlackEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SlackProducer {

    @Value("${spring.kafka.topic.slack}")
    private String topic;

    private final KafkaTemplate<Long, SlackEventDto> kafkaTemplate;

    public void send(SlackEventDto eventDto) {
        kafkaTemplate.send(topic, eventDto);
        log.info("[produce] message={}", eventDto.getMessage());
    }
}
