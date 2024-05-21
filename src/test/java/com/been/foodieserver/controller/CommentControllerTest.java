package com.been.foodieserver.controller;

import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CommentDto;
import com.been.foodieserver.dto.request.CommentRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.CommentResponse;
import com.been.foodieserver.fixture.CommentFixture;
import com.been.foodieserver.fixture.PostFixture;
import com.been.foodieserver.fixture.UserFixture;
import com.been.foodieserver.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CommentService commentService;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    @WithMockUser
    @DisplayName("요청이 유효하면 댓글 작성 성공")
    @Test
    void writeComment_IfRequestIsValid() throws Exception {
        //Given
        CommentRequest request = new CommentRequest("content");

        Post post = PostFixture.get("title", "user1", "자유 게시판");
        User user = UserFixture.get(2L, "user");
        Comment comment = CommentFixture.get(user, post, 1L, request.getContent());

        CommentResponse response = CommentResponse.of(comment);

        when(commentService.writeComment(eq(user.getLoginId()), eq(post.getId()), any(CommentDto.class))).thenReturn(response);

        //When & Then
        mockMvc.perform(post(baseUrl + "/posts/" + post.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.postId").value(response.getPostId()))
                .andExpect(jsonPath("$.data.content").value(response.getContent()));

        then(commentService).should().writeComment(eq(user.getLoginId()), eq(post.getId()), any(CommentDto.class));
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 댓글 수정 성공")
    @Test
    void modifyComment_IfRequestIsValid() throws Exception {
        //Given
        CommentRequest request = new CommentRequest("modify content");

        Post post = PostFixture.get("title", "user1", "자유 게시판");
        User user = UserFixture.get(2L, "user");
        Comment comment = CommentFixture.get(user, post, 1L, request.getContent());

        CommentResponse response = CommentResponse.of(comment);

        when(commentService.modifyComment(eq(user.getLoginId()), eq(post.getId()), eq(comment.getId()), any(CommentDto.class))).thenReturn(response);

        //When & Then
        mockMvc.perform(put(baseUrl + "/posts/" + post.getId() + "/comments/" + comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.postId").value(response.getPostId()))
                .andExpect(jsonPath("$.data.content").value(response.getContent()));

        then(commentService).should().modifyComment(eq(user.getLoginId()), eq(post.getId()), eq(comment.getId()), any(CommentDto.class));
    }
}
