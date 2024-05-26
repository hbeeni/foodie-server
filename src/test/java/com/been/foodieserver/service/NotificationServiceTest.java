package com.been.foodieserver.service;

import com.been.foodieserver.domain.Notification;
import com.been.foodieserver.domain.NotificationType;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.response.NotificationResponse;
import com.been.foodieserver.fixture.NotificationFixture;
import com.been.foodieserver.fixture.UserFixture;
import com.been.foodieserver.repository.NotificationRepository;
import com.been.foodieserver.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @DisplayName("알림 목록 조회 요청이 유효하면 알림 목록 조회 성공")
    @Test
    void getNotificationList_IfRequestIsValid() {
        //Given
        User receiver = UserFixture.get(1L, "receiver");
        User fromUser1 = UserFixture.get(2L, "from1");
        User fromUser2 = UserFixture.get(3L, "from2");

        Notification notification1 = NotificationFixture.get(1L, receiver, NotificationType.NEW_COMMENT_ON_POST, fromUser1.getId(), 1L);
        Notification notification2 = NotificationFixture.get(2L, receiver, NotificationType.NEW_FOLLOW, fromUser2.getId(), 1L);

        List<Notification> content = List.of(notification2, notification1);
        Page<Notification> notificationPage = new PageImpl<>(content);

        int pageNum = 1;
        int pageSize = content.size();

        given(notificationRepository.findAllWithReceiverByReceiver_LoginId(any(Pageable.class), eq(receiver.getLoginId()))).willReturn(notificationPage);
        given(userRepository.findAllById(List.of(fromUser2.getId(), fromUser1.getId()))).willReturn(List.of(fromUser2, fromUser1));

        //When
        Page<NotificationResponse> result = notificationService.getNotificationList(receiver.getLoginId(), pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getToUser().getLoginId()).isEqualTo(receiver.getLoginId());
        assertThat(result.getContent().get(0).getContent()).isEqualTo(String.format(notification2.getType().getNotificationText(), fromUser2.getNickname()));
        assertThat(result.getContent().get(1).getContent()).isEqualTo(String.format(notification1.getType().getNotificationText(), fromUser1.getNickname()));

        then(notificationRepository).should().findAllWithReceiverByReceiver_LoginId(any(Pageable.class), eq(receiver.getLoginId()));
        then(userRepository).should().findAllById(List.of(fromUser2.getId(), fromUser1.getId()));
    }

    @DisplayName("알림 목록 조회 시 알림을 발생시킨 유저가 조회되지 않으면 Unknown User로 표기")
    @Test
    void markUnknownUser_IfFromUserNotFound_WhenGettingNotificationList() {
        //Given
        User receiver = UserFixture.get(1L, "receiver");
        User fromUser1 = UserFixture.get(2L, "from1");
        Long UnknownFromUserId = 3L;

        Notification notification1 = NotificationFixture.get(1L, receiver, NotificationType.NEW_COMMENT_ON_POST, fromUser1.getId(), 1L);
        Notification notification2 = NotificationFixture.get(2L, receiver, NotificationType.NEW_FOLLOW, UnknownFromUserId, 1L);

        List<Notification> content = List.of(notification2, notification1);
        Page<Notification> notificationPage = new PageImpl<>(content);

        int pageNum = 1;
        int pageSize = content.size();

        given(notificationRepository.findAllWithReceiverByReceiver_LoginId(any(Pageable.class), eq(receiver.getLoginId()))).willReturn(notificationPage);
        given(userRepository.findAllById(List.of(UnknownFromUserId, fromUser1.getId()))).willReturn(List.of(fromUser1));

        //When
        Page<NotificationResponse> result = notificationService.getNotificationList(receiver.getLoginId(), pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getToUser().getLoginId()).isEqualTo(receiver.getLoginId());
        assertThat(result.getContent().get(0).getContent()).isEqualTo(String.format(notification2.getType().getNotificationText(), "Unknown User"));
        assertThat(result.getContent().get(1).getContent()).isEqualTo(String.format(notification1.getType().getNotificationText(), fromUser1.getNickname()));

        then(notificationRepository).should().findAllWithReceiverByReceiver_LoginId(any(Pageable.class), eq(receiver.getLoginId()));
        then(userRepository).should().findAllById(List.of(UnknownFromUserId, fromUser1.getId()));
    }
}
