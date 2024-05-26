package com.been.foodieserver.service;

import com.been.foodieserver.domain.Notification;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.response.NotificationResponse;
import com.been.foodieserver.repository.NotificationRepository;
import com.been.foodieserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Page<NotificationResponse> getNotificationList(String loginId, int pageNum, int pageSize) {
        Pageable pageable = makePageable(pageNum, pageSize);
        Page<Notification> notifications = notificationRepository.findAllWithReceiverByReceiver_LoginId(pageable, loginId);

        //알림을 발생시킨 유저 id 리스트 생성
        List<Long> userIds = notifications.getContent().stream()
                .map(Notification::getFromUserId)
                .toList();

        //알림을 발생시킨 유저 id:nickname Map 생성
        Map<Long, String> userIdToNicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return notifications.map(notification ->
                NotificationResponse.of(notification, userIdToNicknameMap.getOrDefault(notification.getFromUserId(), "Unknown User")));
    }

    private static PageRequest makePageable(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
