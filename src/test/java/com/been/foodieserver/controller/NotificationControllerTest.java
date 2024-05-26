package com.been.foodieserver.controller;

import com.been.foodieserver.domain.Notification;
import com.been.foodieserver.domain.NotificationType;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.NotificationResponse;
import com.been.foodieserver.fixture.NotificationFixture;
import com.been.foodieserver.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String notificationApi;
    private User receiver;

    @BeforeEach
    void setUp() {
        notificationApi = baseUrl + "/notifications";
        receiver = User.of("receiver", "pwd", "receiver", Role.USER);
    }

    @WithMockUser("receiver")
    @DisplayName("요청이 유효하면 알림 목록 조회 성공")
    @Test
    void getNotificationList_IfRequestIsValid() throws Exception {
        //Given
        Notification notification1 = NotificationFixture.get(1L, receiver, NotificationType.NEW_COMMENT_ON_POST, 2L, 1L);
        Notification notification2 = NotificationFixture.get(2L, receiver, NotificationType.NEW_FOLLOW, 3L, 1L);

        String fromUserNickname1 = "from1";
        String fromUserNickname2 = "from2";
        NotificationResponse notificationResponse1 = NotificationResponse.of(notification1, fromUserNickname1);
        NotificationResponse notificationResponse2 = NotificationResponse.of(notification2, fromUserNickname2);

        Page<NotificationResponse> notificationResponsePage = new PageImpl<>(List.of(notificationResponse2, notificationResponse1));

        int pageNum = 1;
        int pageSize = notificationResponsePage.getSize();

        when(notificationService.getNotificationList(receiver.getLoginId(), pageNum, pageSize)).thenReturn(notificationResponsePage);

        //When & Then
        mockMvc.perform(get(notificationApi)
                        .param("pageNum", String.valueOf(pageNum))
                        .param("pageSize", String.valueOf(pageSize))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(notification2.getId()))
                .andExpect(jsonPath("$.data[0].toUser.loginId").value(receiver.getLoginId()))
                .andExpect(jsonPath("$.data[0].content").value(String.format(notification2.getType().getNotificationText(), fromUserNickname2)))
                .andExpect(jsonPath("$.data[1].content").value(String.format(notification1.getType().getNotificationText(), fromUserNickname1)))
                .andExpect(jsonPath("$.pagination").exists())
                .andExpect(jsonPath("$.pagination.currentPage").value(pageNum))
                .andExpect(jsonPath("$.pagination.pageSize").value(pageSize));

        then(notificationService).should().getNotificationList(receiver.getLoginId(), pageNum, pageSize);
    }
}
