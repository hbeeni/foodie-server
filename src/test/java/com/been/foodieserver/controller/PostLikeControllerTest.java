package com.been.foodieserver.controller;

import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.LikeResponse;
import com.been.foodieserver.service.PostLikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class PostLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostLikeService postLikeService;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    @WithMockUser
    @DisplayName("요청이 유효하면 좋아요 성공")
    @Test
    void like_IfRequestIsValid() throws Exception {
        //Given
        String loginId = "user";
        long postId = 1L;

        LikeResponse response = LikeResponse.of(loginId, postId);

        when(postLikeService.like(loginId, postId)).thenReturn(response);

        //When & Then
        mockMvc.perform(post(baseUrl + "/posts/" + postId + "/likes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.userLoginId").value(loginId))
                .andExpect(jsonPath("$.data.postId").exists());

        then(postLikeService).should().like(loginId, postId);
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 좋아요 취소 성공")
    @Test
    void unlike_IfRequestIsValid() throws Exception {
        //Given
        String loginId = "user";
        long postId = 1L;

        willDoNothing().given(postLikeService).unlike(loginId, postId);

        //When & Then
        mockMvc.perform(delete(baseUrl + "/posts/" + postId + "/likes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(postLikeService).should().unlike(loginId, postId);
    }
}
