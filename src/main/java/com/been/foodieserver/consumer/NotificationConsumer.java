package com.been.foodieserver.consumer;

import com.been.foodieserver.dto.NotificationEventDto;
import com.been.foodieserver.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationConsumer {

    private final SseService sseService;

    @KafkaListener(topics = "${spring.kafka.topic.notification}", groupId = "notification")
    public void consume(NotificationEventDto eventDto, Acknowledgment ack) {
        log.info("[consume the event] {}", eventDto);
        sseService.saveNotificationAndSendToClient(eventDto.getReceiver().toEntity(),
                eventDto.getType(),
                eventDto.getFromUser().toEntity(),
                eventDto.getTargetId());
        ack.acknowledge();
    }
}
