package com.been.foodieserver.repository;

import com.been.foodieserver.config.JpaConfig;
import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaConfig.class)
@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Category category;
    private Post post;
    private Comment comment;

    @BeforeEach
    public void setUp() {
        user = User.of("user", "pwd", "user", Role.USER);
        em.persist(user);

        category = Category.of("category", null);
        em.persist(category);

        post = Post.of(user, category, "title", "content");
        em.persist(post);

        comment = Comment.of(post, user, "comment");
        em.persist(comment);
    }

    @DisplayName("댓글을 조회(페치 조인)하면 유저, 게시글, 카테고리가 페치 조인된 댓글 결과 반환")
    @Test
    void returnCommentWithUserAndPostAndCategory_WhenFetched() {
        //When
        Optional<Comment> result = commentRepository.findWithUserAndPostAndCategoryByIdAndLoginIdAndPostId(comment.getId(), user.getLoginId(), post.getId());

        //Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(comment.getId());
        assertThat(result.get().getUser().getLoginId()).isEqualTo(user.getLoginId());
        assertThat(result.get().getPost().getId()).isEqualTo(post.getId());
        assertThat(result.get().getPost().getCategory().getName()).isEqualTo(category.getName());
    }

    @DisplayName("댓글 목록을 조회(페치 조인)하면 유저, 게시글, 카테고리가 페치 조인된 댓글 목록 결과 반환")
    @Test
    void returnCommentListWithUserAndPostAndCategory_WhenFetched() {
        //Given
        Comment comment2 = Comment.of(post, user, "comment2");
        Comment comment3 = Comment.of(post, user, "comment3");

        em.persist(comment2);
        em.persist(comment3);

        em.flush();
        em.clear();

        int pageNum = 0;
        int pageSize = 10;

        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        //When
        Page<Comment> result = commentRepository.findAllWithUserAndPostAndCategoryByPostId(pageable, post.getId());

        //Then
        List<Comment> resultContent = result.getContent();
        Comment firstComment = resultContent.get(0);

        assertThat(resultContent).hasSize(3);
        assertThat(firstComment.getUser().getLoginId()).isEqualTo(user.getLoginId());
        assertThat(firstComment.getPost().getId()).isEqualTo(post.getId());
        assertThat(firstComment.getPost().getCategory().getName()).isEqualTo(category.getName());
    }
}
