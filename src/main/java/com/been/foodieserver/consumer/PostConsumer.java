package com.been.foodieserver.consumer;

import com.been.foodieserver.domain.Post;
import com.been.foodieserver.repository.cache.PostCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PostConsumer {

    private final PostCacheRepository postCacheRepository;

    @KafkaListener(topics = "${spring.kafka.topic.post}", groupId = "post")
    public void consume(Post post, Acknowledgment ack) {
        log.info("[consume the event] postId={}", post.getId());
        postCacheRepository.save(post);
        ack.acknowledge();
    }
}
