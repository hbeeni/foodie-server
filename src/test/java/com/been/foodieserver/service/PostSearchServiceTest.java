package com.been.foodieserver.service;

import com.been.foodieserver.domain.Post;
import com.been.foodieserver.dto.PostSearchDto;
import com.been.foodieserver.dto.PostSearchType;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.fixture.PostFixture;
import com.been.foodieserver.repository.PostQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostSearchServiceTest {

    @Mock
    private PostQueryRepository postQueryRepository;

    @InjectMocks
    private PostSearchService postSearchService;

    @DisplayName("검색 요청이 유효하면 게시글 검색 성공")
    @Test
    void searchPost_IfRequestIsValid() {
        //Given
        String searchTitle = "searchTitle";

        Post post1 = PostFixture.get("title1", "writer", "자유 게시판");
        Post post2 = PostFixture.get("title2", "writer", "자유 게시판");

        int pageNum = 1;
        int pageSize = 10;

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        List<Post> content = List.of(post2, post1);
        Page<Post> postPage = new PageImpl<>(content, pageable, content.size());

        PostSearchDto dto = PostSearchDto.builder()
                .searchType(PostSearchType.TITLE)
                .keyword(searchTitle)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();

        given(postQueryRepository.findAllByTitleContainsIgnoreCase(dto)).willReturn(postPage);

        //When
        Page<PostResponse> result = postSearchService.search(dto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(post2.getTitle());

        then(postQueryRepository).should().findAllByTitleContainsIgnoreCase(dto);
    }
}
