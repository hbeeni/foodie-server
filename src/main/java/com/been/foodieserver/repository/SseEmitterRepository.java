package com.been.foodieserver.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class SseEmitterRepository {

    private Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public SseEmitter save(Long userId, SseEmitter sseEmitter) {
        log.info("[Set sseEmitter] userId={}", userId);
        sseEmitterMap.put(userId, sseEmitter);
        return sseEmitter;
    }

    public Optional<SseEmitter> get(Long userId) {
        log.info("[Get sseEmitter] userId={}", userId);
        return Optional.ofNullable(sseEmitterMap.get(userId));
    }

    public void deleteEmitterByUserId(Long userId) {
        sseEmitterMap.remove(userId);
    }
}
