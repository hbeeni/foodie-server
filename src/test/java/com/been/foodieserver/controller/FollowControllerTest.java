package com.been.foodieserver.controller;

import com.been.foodieserver.domain.Follow;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.FollowResponse;
import com.been.foodieserver.dto.response.FollowerResponse;
import com.been.foodieserver.fixture.FollowFixture;
import com.been.foodieserver.service.FollowService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String followApi;

    @BeforeEach
    void setUp() {
        followApi = baseUrl + "/follows";
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 해당 유저를 팔로우한 유저 목록 조회 성공")
    @Test
    void getFollowerList_IfRequestIsValid() throws Exception {
        //Given
        String loginId = "user";
        Follow follow1 = FollowFixture.get(1L, 2L, "follower1", 1L, loginId);
        Follow follow2 = FollowFixture.get(2L, 3L, "follower2", 1L, loginId);

        FollowerResponse response1 = FollowerResponse.of(follow1);
        FollowerResponse response2 = FollowerResponse.of(follow2);

        Page<FollowerResponse> followerResponsePage = new PageImpl<>(List.of(response2, response1));

        int pageNum = 1;
        int pageSize = 10;

        when(followService.getFollowerList(loginId, pageNum, pageSize)).thenReturn(followerResponsePage);

        //When & Then
        mockMvc.perform(get(followApi)
                        .param("pageNum", String.valueOf(pageNum))
                        .param("pageSize", String.valueOf(pageSize))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].loginId").value(response2.getLoginId()))
                .andExpect(jsonPath("$.data[1].loginId").value(response1.getLoginId()));

        then(followService).should().getFollowerList(loginId, pageNum, pageSize);
    }

    @WithMockUser("follower")
    @DisplayName("요청이 유효하면 팔로우 성공")
    @Test
    void followUser_IfRequestIsValid() throws Exception {
        //Given
        String followerLoginId = "follower";
        String followeeLoginId = "followee";
        FollowResponse response = FollowResponse.follow(followerLoginId, followeeLoginId);

        when(followService.follow(followerLoginId, followeeLoginId)).thenReturn(response);

        //When & Then
        mockMvc.perform(post(followApi + "/" + followeeLoginId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").value(FollowResponse.STATUS_FOLLOW))
                .andExpect(jsonPath("$.data.follower").value(followerLoginId))
                .andExpect(jsonPath("$.data.followee").value(followeeLoginId));

        then(followService).should().follow(followerLoginId, followeeLoginId);
    }

    @WithMockUser("follower")
    @DisplayName("요청이 유효하면 언팔로우 성공")
    @Test
    void unfollowUser_IfRequestIsValid() throws Exception {
        //Given
        String followerLoginId = "follower";
        String followeeLoginId = "followee";
        FollowResponse response = FollowResponse.unfollow(followerLoginId, followeeLoginId);

        when(followService.unfollow(followerLoginId, followeeLoginId)).thenReturn(response);

        //When & Then
        mockMvc.perform(delete(followApi + "/" + followeeLoginId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").value(FollowResponse.STATUS_UNFOLLOW))
                .andExpect(jsonPath("$.data.follower").value(followerLoginId))
                .andExpect(jsonPath("$.data.followee").value(followeeLoginId));

        then(followService).should().unfollow(followerLoginId, followeeLoginId);
    }
}
