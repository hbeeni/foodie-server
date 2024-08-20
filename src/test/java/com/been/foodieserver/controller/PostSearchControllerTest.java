package com.been.foodieserver.controller;

import com.been.foodieserver.domain.Post;
import com.been.foodieserver.dto.PostSearchDto;
import com.been.foodieserver.dto.PostSearchType;
import com.been.foodieserver.dto.request.PostSearchRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.fixture.PostFixture;
import com.been.foodieserver.service.PostSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class PostSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private PostSearchService postSearchService;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    @WithMockUser
    @DisplayName("요청이 유효하면 게시글 검색 성공")
    @Test
    void searchPost_IfRequestIsValid() throws Exception {
        //Given
        int pageNum = 1;
        int pageSize = 10;

        PostSearchRequest request = new PostSearchRequest(PostSearchType.TITLE, "title", pageNum, pageSize);

        Post post1 = PostFixture.get("title1", "writer", "자유 게시판");
        Post post2 = PostFixture.get("title2", "writer", "자유 게시판");

        PostResponse postResponse1 = PostResponse.of(post1);
        PostResponse postResponse2 = PostResponse.of(post2);

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        List<PostResponse> content = List.of(postResponse2, postResponse1);

        Page<PostResponse> postResponsePage = new PageImpl<>(content, pageable, content.size());

        when(postSearchService.search(any(PostSearchDto.class))).thenReturn(postResponsePage);

        //When & Then
        mockMvc.perform(get(baseUrl + "/posts/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value(post2.getTitle()))
                .andExpect(jsonPath("$.data[0].writer.loginId").value(post2.getUser().getLoginId()))
                .andExpect(jsonPath("$.pagination").exists())
                .andExpect(jsonPath("$.pagination.currentPage").value(pageNum))
                .andExpect(jsonPath("$.pagination.pageSize").value(pageSize));

        then(postSearchService).should().search(any(PostSearchDto.class));
    }
}
