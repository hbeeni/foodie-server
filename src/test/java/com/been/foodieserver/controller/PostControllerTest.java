package com.been.foodieserver.controller;

import com.been.foodieserver.domain.Post;
import com.been.foodieserver.dto.PostDto;
import com.been.foodieserver.dto.request.PostWriteRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.fixture.PostFixture;
import com.been.foodieserver.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private PostService postService;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String postApi;

    @BeforeEach
    void setUp() {
        postApi = baseUrl + "/posts";
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 게시글 목록 조회 성공")
    @Test
    void getPostList_IfRequestIsValid() throws Exception {
        //Given
        Post post1 = PostFixture.get("title1", "user", "자유 게시판");
        Post post2 = PostFixture.get("title2", "user", "자유 게시판");

        PostResponse postResponse1 = PostResponse.of(post1);
        PostResponse postResponse2 = PostResponse.of(post2);

        Page<PostResponse> postResponsePage = new PageImpl<>(List.of(postResponse2, postResponse1));

        int pageNum = 1;
        int pageSize = postResponsePage.getSize();

        when(postService.getPostList(pageNum, pageSize)).thenReturn(postResponsePage);

        //When & Then
        mockMvc.perform(get(postApi)
                        .param("pageNum", String.valueOf(pageNum))
                        .param("pageSize", String.valueOf(pageSize))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value(post2.getTitle()))
                .andExpect(jsonPath("$.pagination").exists())
                .andExpect(jsonPath("$.pagination.currentPage").value(pageNum))
                .andExpect(jsonPath("$.pagination.pageSize").value(pageSize));

        then(postService).should().getPostList(pageNum, pageSize);
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 내 게시글 목록 조회 성공")
    @Test
    void getMyPostList_IfRequestIsValid() throws Exception {
        //Given
        String loginId = "user";
        Post post1 = PostFixture.get("title1", loginId, "자유 게시판");
        Post post2 = PostFixture.get("title2", loginId, "자유 게시판");

        PostResponse postResponse1 = PostResponse.of(post1);
        PostResponse postResponse2 = PostResponse.of(post2);

        Page<PostResponse> postResponsePage = new PageImpl<>(List.of(postResponse2, postResponse1));

        int pageNum = 1;
        int pageSize = postResponsePage.getSize();

        when(postService.getMyPostList(loginId, pageNum, pageSize)).thenReturn(postResponsePage);

        //When & Then
        mockMvc.perform(get(postApi + "/my")
                        .param("pageNum", String.valueOf(pageNum))
                        .param("pageSize", String.valueOf(pageSize))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value(post2.getTitle()))
                .andExpect(jsonPath("$.pagination").exists())
                .andExpect(jsonPath("$.pagination.currentPage").value(pageNum))
                .andExpect(jsonPath("$.pagination.pageSize").value(pageSize));

        then(postService).should().getMyPostList(loginId, pageNum, pageSize);
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 게시글 조회 성공")
    @Test
    void getPost_IfRequestIsValid() throws Exception {
        //Given
        Post post = PostFixture.get("title", "user", "자유 게시판");
        Long postId = post.getId();

        PostResponse response = PostResponse.of(post.getUser(), post.getCategory(), post);

        when(postService.getPost(postId)).thenReturn(response);

        //When & Then
        mockMvc.perform(get(postApi + "/" + postId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.title").value(response.getTitle()))
                .andExpect(jsonPath("$.data.writer.loginId").value(response.getWriter().getLoginId()));

        then(postService).should().getPost(postId);
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 게시글 작성 성공")
    @Test
    void writePost_IfRequestIsValid() throws Exception {
        //Given
        PostWriteRequest request = new PostWriteRequest(1L, "title", "content");
        Post post = PostFixture.get(request.getTitle(), "user", "자유 게시판");
        PostResponse response = PostResponse.of(post.getUser(), post.getCategory(), post);

        when(postService.writePost(eq(post.getUser().getLoginId()), any(PostDto.class))).thenReturn(response);

        //When & Then
        mockMvc.perform(post(postApi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.title").value(response.getTitle()))
                .andExpect(jsonPath("$.data.writer.loginId").value(response.getWriter().getLoginId()));

        then(postService).should().writePost(eq(post.getUser().getLoginId()), any(PostDto.class));
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 게시글 수정 성공")
    @Test
    void modifyPost_IfRequestIsValid() throws Exception {
        //Given
        PostWriteRequest request = new PostWriteRequest(1L, "title 수정", "content 수정");
        Post post = PostFixture.get(request.getTitle(), "user", "자유 게시판");
        Long postId = post.getId();
        PostResponse response = PostResponse.of(post.getUser(), post.getCategory(), post);

        when(postService.modifyPost(eq(post.getUser().getLoginId()), eq(postId), any(PostDto.class))).thenReturn(response);

        //When & Then
        mockMvc.perform(put(postApi + "/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.title").value(response.getTitle()))
                .andExpect(jsonPath("$.data.content").value(response.getContent()))
                .andExpect(jsonPath("$.data.writer.loginId").value(response.getWriter().getLoginId()));

        then(postService).should().modifyPost(eq(post.getUser().getLoginId()), eq(postId), any(PostDto.class));
    }

    @WithMockUser
    @DisplayName("요청이 유효하면 게시글 삭제 성공")
    @Test
    void deletePost_IfRequestIsValid() throws Exception {
        //Given
        Post post = PostFixture.get("title", "user", "자유 게시판");
        Long postId = post.getId();
        ReflectionTestUtils.setField(post, "deletedAt", Timestamp.valueOf(LocalDateTime.now()));

        PostResponse response = PostResponse.of(post.getUser(), post.getCategory(), post);

        when(postService.deletePost(post.getUser().getLoginId(), postId)).thenReturn(response);

        //When & Then
        mockMvc.perform(delete(postApi + "/" + postId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.title").value(response.getTitle()))
                .andExpect(jsonPath("$.data.content").value(response.getContent()))
                .andExpect(jsonPath("$.data.writer.loginId").value(response.getWriter().getLoginId()))
                .andExpect(jsonPath("$.data.deletedAt").isNotEmpty());

        then(postService).should().deletePost(post.getUser().getLoginId(), postId);
    }
}
