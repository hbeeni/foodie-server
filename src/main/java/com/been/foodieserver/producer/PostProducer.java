package com.been.foodieserver.producer;

import com.been.foodieserver.domain.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PostProducer {

    @Value("${spring.kafka.topic.post}")
    private String topic;

    private final KafkaTemplate<Long, Post> kafkaTemplate;

    public void send(Post post) {
        kafkaTemplate.send(topic, post);
        log.info("[produce] postId={}", post.getId());
    }
}
