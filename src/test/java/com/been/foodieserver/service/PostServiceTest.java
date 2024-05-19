package com.been.foodieserver.service;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.PostDto;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.fixture.PostFixture;
import com.been.foodieserver.repository.CategoryRepository;
import com.been.foodieserver.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Post post;
    private Category category;
    private User user;
    private PostDto postDto;

    @BeforeEach
    void setUp() {
        post = PostFixture.get("title", "user1", "자유 게시판");
        category = post.getCategory();
        user = post.getUser();

        postDto = PostDto.builder()
                .categoryId(post.getCategory().getId())
                .title(post.getTitle())
                .content(post.getContent())
                .build();
    }

    @DisplayName("게시글 목록 요청이 유효하면 게시글 목록 조회 성공")
    @Test
    void getPostList_IfRequestIsValid() {
        //Given
        Post post1 = PostFixture.get("title1", "user", "자유 게시판");
        Post post2 = PostFixture.get("title2", "user", "자유 게시판");

        List<Post> content = List.of(post2, post1);
        Page<Post> postPage = new PageImpl<>(content);

        int pageNum = 1;
        int pageSize = content.size();

        given(postRepository.findAll(any(Pageable.class))).willReturn(postPage);

        //When
        Page<PostResponse> result = postService.getPostList(pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(post2.getTitle());

        then(postRepository).should().findAll(any(Pageable.class));
        then(userService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 요청이 유효하면 게시글 조회 성공")
    @Test
    void getPost_IfRequestIsValid() {
        //Given
        given(postRepository.findWithFetchJoinById(post.getId())).willReturn(Optional.of(post));

        //When
        PostResponse result = postService.getPost(post.getId());

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getCategoryName()).isEqualTo(category.getName());
        assertThat(result.getWriter()).isNotNull();
        assertThat(result.getWriter().getLoginId()).isEqualTo(user.getLoginId());

        then(postRepository).should().findWithFetchJoinById(post.getId());
        then(userService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("조회할 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist_WhenGettingPost() {
        //Given
        Long postId = post.getId();

        given(postRepository.findWithFetchJoinById(postId)).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> postService.getPost(postId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(postRepository).should().findWithFetchJoinById(postId);
        then(userService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 요청이 유효하면 게시글 작성 성공")
    @Test
    void writePost_IfRequestIsValid() {
        //Given
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(userService.getUserOrException(user.getLoginId())).willReturn(user);
        given(postRepository.save(any(Post.class))).willReturn(post);

        //When
        PostResponse result = postService.writePost(user.getLoginId(), postDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getPostId()).isNotNull();
        assertThat(result.getCategoryName()).isEqualTo(category.getName());
        assertThat(result.getWriter()).isNotNull();
        assertThat(result.getWriter().getLoginId()).isEqualTo(user.getLoginId());

        then(categoryRepository).should().findById(category.getId());
        then(userService).should().getUserOrException(user.getLoginId());
        then(postRepository).should().save(any(Post.class));
    }

    @DisplayName("카테고리가 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfCategoryDoesntExist() {
        //Given
        String loginId = user.getLoginId();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> postService.writePost(loginId, postDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getMessage());

        //Then
        then(categoryRepository).should().findById(category.getId());
        then(userService).shouldHaveNoInteractions();
        then(postRepository).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 수정 요청이 유효하면 게시글 수정 성공")
    @Test
    void modifyPost_IfRequestIsValid() {
        //Given
        category = Category.of("Q&A", "Q&A");
        ReflectionTestUtils.setField(category, "id", 2L);

        postDto = PostDto.builder()
                .categoryId(category.getId())
                .title("title 수정")
                .content("content 수정")
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(postRepository.findByIdAndUser_LoginId(post.getId(), user.getLoginId())).willReturn(Optional.of(post));
        given(userService.getUserOrException(user.getLoginId())).willReturn(user);
        willDoNothing().given(postRepository).flush();

        //When
        PostResponse result = postService.modifyPost(user.getLoginId(), post.getId(), postDto);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getCategoryName()).isEqualTo(category.getName());
        assertThat(result.getWriter()).isNotNull();
        assertThat(result.getWriter().getLoginId()).isEqualTo(user.getLoginId());
        assertThat(result.getTitle()).isEqualTo(postDto.getTitle());
        assertThat(result.getContent()).isEqualTo(postDto.getContent());

        then(categoryRepository).should().findById(category.getId());
        then(postRepository).should().findByIdAndUser_LoginId(post.getId(), user.getLoginId());
        then(userService).should().getUserOrException(user.getLoginId());
        then(postRepository).should().flush();
    }

    @DisplayName("카테고리가 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfCategoryDoesntExist_WhenModifyingPost() {
        //Given
        String loginId = user.getLoginId();
        Long postId = post.getId();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> postService.modifyPost(loginId, postId, postDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CATEGORY_NOT_FOUND.getMessage());

        //Then
        then(categoryRepository).should().findById(category.getId());
        then(postRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("수정할 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist_WhenModifyingPost() {
        //Given
        String loginId = user.getLoginId();
        Long postId = post.getId();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(postRepository.findByIdAndUser_LoginId(postId, user.getLoginId())).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> postService.modifyPost(loginId, postId, postDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(categoryRepository).should().findById(category.getId());
        then(postRepository).should().findByIdAndUser_LoginId(postId, loginId);
        then(userService).shouldHaveNoInteractions();
        then(postRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("게시글 삭제 요청이 유효하면 게시글 삭제 성공")
    @Test
    void deletePost_IfRequestIsValid() {
        //Given
        given(postRepository.findWithFetchJoinByIdAndUser_LoginId(post.getId(), user.getLoginId())).willReturn(Optional.of(post));
        willDoNothing().given(postRepository).flush();

        //When
        PostResponse result = postService.deletePost(user.getLoginId(), post.getId());

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getWriter()).isNotNull();
        assertThat(result.getTitle()).isNotNull();
        assertThat(result.getDeletedAt()).isNotNull();

        then(postRepository).should().findWithFetchJoinByIdAndUser_LoginId(post.getId(), user.getLoginId());
        then(postRepository).should().flush();
        then(categoryRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("삭제할 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist_WhenDeletingPost() {
        //Given
        String loginId = user.getLoginId();
        Long postId = post.getId();

        given(postRepository.findWithFetchJoinByIdAndUser_LoginId(postId, user.getLoginId())).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> postService.deletePost(loginId, postId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(postRepository).should().findWithFetchJoinByIdAndUser_LoginId(postId, loginId);
        then(postRepository).shouldHaveNoMoreInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }
}