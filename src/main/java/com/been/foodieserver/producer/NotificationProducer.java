package com.been.foodieserver.producer;

import com.been.foodieserver.dto.NotificationEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationProducer {

    @Value("${spring.kafka.topic.notification}")
    private String topic;

    private final KafkaTemplate<Long, NotificationEventDto> kafkaTemplate;

    public void send(NotificationEventDto eventDto) {
        kafkaTemplate.send(topic, eventDto.getReceiver().getId(), eventDto);
        log.info("[produce] receiver Id={}", eventDto.getReceiver().getId());
    }
}
