package com.been.foodieserver.service;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Like;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.PostDto;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.fixture.PostFixture;
import com.been.foodieserver.fixture.UserFixture;
import com.been.foodieserver.repository.CategoryRepository;
import com.been.foodieserver.repository.LikeRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private FollowService followService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LikeRepository likeRepository;

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

        given(postRepository.findAllWithUserAndCategory(any(Pageable.class))).willReturn(postPage);

        //When
        Page<PostResponse> result = postService.getPostList(pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(post2.getTitle());

        then(postRepository).should().findAllWithUserAndCategory(any(Pageable.class));
        then(userService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("내가 작성한 게시글 목록 요청이 유효하면 내 게시글 목록 조회 성공")
    @Test
    void getMyPostList_IfRequestIsValid() {
        //Given
        String loginId = "user";
        Post post1 = PostFixture.get(1L, "title1", loginId, "자유 게시판");
        Post post2 = PostFixture.get(2L, "title2", loginId, "자유 게시판");

        List<Post> content = List.of(post2, post1);
        Page<Post> postPage = new PageImpl<>(content);

        int pageNum = 1;
        int pageSize = content.size();

        given(postRepository.findAllWithUserAndCategoryByUser_LoginId(any(Pageable.class), eq(loginId))).willReturn(postPage);

        //When
        Page<PostResponse> result = postService.getMyPostList(loginId, pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getPostId()).isEqualTo(post2.getId());

        then(postRepository).should().findAllWithUserAndCategoryByUser_LoginId(any(Pageable.class), eq(loginId));
        then(userService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("다른 사용자가 작성한 게시글 목록 요청이 유효하면 다른 사용자 게시글 목록 조회 성공")
    @Test
    void getPostListByUser_IfRequestIsValid() {
        //Given
        String loginId = "writer";
        Post post1 = PostFixture.get(1L, "title1", loginId, "자유 게시판");
        Post post2 = PostFixture.get(2L, "title2", loginId, "자유 게시판");

        List<Post> content = List.of(post2, post1);
        Page<Post> postPage = new PageImpl<>(content);

        int pageNum = 1;
        int pageSize = content.size();

        given(userService.isLoginIdExist(loginId)).willReturn(true);
        given(postRepository.findAllWithUserAndCategoryByUser_LoginId(any(Pageable.class), eq(loginId))).willReturn(postPage);

        //When
        Page<PostResponse> result = postService.getPostListByUserLoginId(loginId, pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getPostId()).isEqualTo(post2.getId());

        then(userService).should().isLoginIdExist(loginId);
        then(postRepository).should().findAllWithUserAndCategoryByUser_LoginId(any(Pageable.class), eq(loginId));
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 사용자의 게시글 목록 요청을 요청하면 예외 발생")
    @Test
    void throwsException_IfUserDoesNotExist_WhenGettingPostList() {
        //Given
        String loginId = "writer";

        given(userService.isLoginIdExist(loginId)).willReturn(false);

        //When & Then
        assertThatThrownBy(() -> postService.getPostListByUserLoginId(loginId, 1, 10))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        then(userService).should().isLoginIdExist(loginId);
        then(postRepository).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("팔로우한 유저의 게시글 목록 조회 요청이 유효하면 팔로우 유저 게시글 목록 조회 성공")
    @Test
    void getPostListByFollowees_IfRequestIsValid() {
        //Given
        String loginId = "follower";
        String followeeLoginId1 = "followee1";
        String followeeLoginId2 = "followee2";
        Set<String> followeeLoginIdSet = Set.of(followeeLoginId1, followeeLoginId2);

        Post post1 = PostFixture.get(1L, "title", followeeLoginId1, "자유 게시판");
        Post post2 = PostFixture.get(2L, "title", followeeLoginId1, "자유 게시판");
        Post post3 = PostFixture.get(3L, "title", followeeLoginId1, "자유 게시판");
        Post post4 = PostFixture.get(4L, "title", followeeLoginId2, "자유 게시판");

        List<Post> content = List.of(post4, post3, post2, post1);
        Page<Post> postPage = new PageImpl<>(content);

        int pageNum = 1;
        int pageSize = content.size();

        given(followService.getFolloweeLoginIds(loginId)).willReturn(followeeLoginIdSet);
        given(postRepository.findAllWithUserAndCategoryByUser_LoginIdIn(any(Pageable.class), eq(followeeLoginIdSet))).willReturn(postPage);

        //When
        Page<PostResponse> result = postService.getPostsByFollowees(loginId, pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getPostId()).isEqualTo(post4.getId());

        then(followService).should().getFolloweeLoginIds(loginId);
        then(postRepository).should().findAllWithUserAndCategoryByUser_LoginIdIn(any(Pageable.class), eq(followeeLoginIdSet));
        then(userService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("팔로우한 유저가 없으면 빈 게시글 목록 반환")
    @Test
    void returnEmptyPostList_IfNoFolloweesExist() {
        //Given
        String loginId = "follower";
        int pageNum = 1;
        int pageSize = 10;

        given(followService.getFolloweeLoginIds(loginId)).willReturn(Set.of());

        //When
        Page<PostResponse> result = postService.getPostsByFollowees(loginId, pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent()).isEmpty();

        then(followService).should().getFolloweeLoginIds(loginId);
        then(postRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("좋아요한 게시글 목록 조회 요청이 유효하면 좋아요한 게시글 목록 조회 성공")
    @Test
    void getLikedPostList_IfRequestIsValid() {
        //Given
        String loginId = user.getLoginId();
        User user2 = UserFixture.get(2L, "user2");

        Post post1 = PostFixture.get(1L, "title", user2.getLoginId(), "자유 게시판");
        Post post2 = PostFixture.get(2L, "title", user2.getLoginId(), "자유 게시판");

        List<Like> likes = new ArrayList<>();
        likes.add(Like.of(user, post1));
        likes.add(Like.of(user, post2));

        List<Long> postIds = List.of(post1.getId(), post2.getId());

        List<Post> content = List.of(post2, post1);
        Page<Post> postPage = new PageImpl<>(content);

        int pageNum = 1;
        int pageSize = content.size();

        given(likeRepository.findByUser_LoginId(loginId)).willReturn(likes);
        given(postRepository.findAllWithUserAndCategoryByIdIn(any(Pageable.class), eq(postIds))).willReturn(postPage);

        //When
        Page<PostResponse> result = postService.getLikedPostList(loginId, pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(content.size());
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent().get(0).getPostId()).isEqualTo(post2.getId());

        then(likeRepository).should().findByUser_LoginId(loginId);
        then(postRepository).should().findAllWithUserAndCategoryByIdIn(any(Pageable.class), eq(postIds));
        then(userService).shouldHaveNoInteractions();
        then(followService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("좋아요한 게시글이 없으면 빈 게시글 목록 반환")
    @Test
    void returnEmptyPostList_IfNoLikedPostExist() {
        //Given
        String loginId = user.getLoginId();
        int pageNum = 1;
        int pageSize = 10;

        given(likeRepository.findByUser_LoginId(loginId)).willReturn(List.of());

        //When
        Page<PostResponse> result = postService.getLikedPostList(loginId, pageNum, pageSize);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getNumber() + 1).isEqualTo(pageNum);
        assertThat(result.getSize()).isEqualTo(pageSize);
        assertThat(result.getContent()).isEmpty();

        then(likeRepository).should().findByUser_LoginId(loginId);
        then(postRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
        then(followService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 요청이 유효하면 게시글 조회 성공")
    @Test
    void getPost_IfRequestIsValid() {
        //Given
        given(postRepository.findWithUserAndCategoryById(post.getId())).willReturn(Optional.of(post));

        //When
        PostResponse result = postService.getPost(post.getId());

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getCategoryName()).isEqualTo(category.getName());
        assertThat(result.getWriter()).isNotNull();
        assertThat(result.getWriter().getLoginId()).isEqualTo(user.getLoginId());

        then(postRepository).should().findWithUserAndCategoryById(post.getId());
        then(userService).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @DisplayName("조회할 게시글이 존재하지 않으면 예외 발생")
    @Test
    void throwsException_IfPostDoesntExist_WhenGettingPost() {
        //Given
        Long postId = post.getId();

        given(postRepository.findWithUserAndCategoryById(postId)).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> postService.getPost(postId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(postRepository).should().findWithUserAndCategoryById(postId);
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
        given(postRepository.findWithUserAndCategoryByIdAndUser_LoginId(post.getId(), user.getLoginId())).willReturn(Optional.of(post));
        willDoNothing().given(postRepository).flush();

        //When
        PostResponse result = postService.deletePost(user.getLoginId(), post.getId());

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getPostId()).isEqualTo(post.getId());
        assertThat(result.getWriter()).isNotNull();
        assertThat(result.getTitle()).isNotNull();
        assertThat(result.getDeletedAt()).isNotNull();

        then(postRepository).should().findWithUserAndCategoryByIdAndUser_LoginId(post.getId(), user.getLoginId());
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

        given(postRepository.findWithUserAndCategoryByIdAndUser_LoginId(postId, user.getLoginId())).willReturn(Optional.empty());

        //When
        assertThatThrownBy(() -> postService.deletePost(loginId, postId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

        //Then
        then(postRepository).should().findWithUserAndCategoryByIdAndUser_LoginId(postId, loginId);
        then(postRepository).shouldHaveNoMoreInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }
}
