package com.been.foodieserver.service;

import com.been.foodieserver.domain.Notification;
import com.been.foodieserver.domain.NotificationType;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.response.NotificationEventResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.NotificationRepository;
import com.been.foodieserver.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private static final String NOTIFICATION_NAME = "notification";

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;

    public void saveNotificationAndSendToClient(User receiver, NotificationType type, User fromUser, Long targetId) {
        Notification notification = saveNotification(receiver, type, fromUser, targetId);
        Long userId = notification.getReceiver().getId();

        sseEmitterRepository.get(userId).ifPresentOrElse(sseEmitter -> {
            try {
                sendToClient(notification.getId().toString(), NotificationEventResponse.of(notification, fromUser.getNickname()), sseEmitter);
            } catch (Exception ex) {
                sseEmitterRepository.deleteEmitterByUserId(userId);
                throw new CustomException(ErrorCode.CONNECTION_ERROR);
            }
        }, () -> log.info("[No emitter founded] userId={}", userId));
    }

    public SseEmitter subscribe(Long userId) {
        SseEmitter sseEmitter = sseEmitterRepository.save(userId, new SseEmitter(DEFAULT_TIMEOUT));

        //완료, 타임아웃, 에러 시 emitter 삭제
        sseEmitter.onCompletion(() -> sseEmitterRepository.deleteEmitterByUserId(userId));
        sseEmitter.onTimeout(() -> sseEmitterRepository.deleteEmitterByUserId(userId));
        sseEmitter.onError(e -> sseEmitterRepository.deleteEmitterByUserId(userId));

        try {
            sendToClient("", "[연결 성공] userId=" + userId, sseEmitter);
        } catch (Exception e) {
            sseEmitterRepository.deleteEmitterByUserId(userId);
            throw new CustomException(ErrorCode.CONNECTION_ERROR);
        }

        return sseEmitter;
    }

    private static void sendToClient(String id, Object data, SseEmitter sseEmitter) throws IOException {
        sseEmitter.send(SseEmitter.event()
                .id(id)
                .name(NOTIFICATION_NAME)
                .data(data));
    }

    private Notification saveNotification(User receiver, NotificationType type, User fromUser, Long targetId) {
        Notification notification = Notification.of(receiver, type, fromUser.getId(), targetId, false);
        return notificationRepository.save(notification);
    }
}
