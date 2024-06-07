package com.been.foodieserver.service;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Like;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.repository.CategoryRepository;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.LikeRepository;
import com.been.foodieserver.repository.PostRepository;
import com.been.foodieserver.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PostServiceIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostService postService;

    @DisplayName("7일 전에 삭제된 게시글 및 게시글과 연관된 데이터를 DB에서 삭제")
    @Transactional
    @Test
    void hardDeletePostsDeleted7DaysAgoAndRelatedData() {
        //Given
        User user = User.of("user", "pwd", "nick1", null, Role.USER);
        User user2 = User.of("user2", "pwd", "nick2", null, Role.USER);
        userRepository.saveAll(List.of(user, user2));

        Category category = Category.of("category", null);
        categoryRepository.save(category);

        em.flush();

        List<Post> posts = new ArrayList<>();
        posts.add(Post.of(user, category, "title", "content")); //delete
        posts.add(Post.of(user, category, "title", "content")); //delete
        posts.add(Post.of(user, category, "title", "content")); //delete
        posts.add(Post.of(user, category, "title", "content")); //delete
        posts.add(Post.of(user, category, "title", "content")); //delete
        posts.add(Post.of(user, category, "title", "content"));
        posts.add(Post.of(user, category, "title", "content"));
        posts.add(Post.of(user, category, "title", "content"));

        ReflectionTestUtils.setField(posts.get(0), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        ReflectionTestUtils.setField(posts.get(1), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        ReflectionTestUtils.setField(posts.get(2), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        ReflectionTestUtils.setField(posts.get(3), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        ReflectionTestUtils.setField(posts.get(4), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(7)));

        postRepository.saveAllAndFlush(posts);

        List<Like> likes = new ArrayList<>();
        likes.add(Like.of(user2, posts.get(0))); //delete
        likes.add(Like.of(user2, posts.get(1))); //delete
        likes.add(Like.of(user2, posts.get(2))); //delete
        likes.add(Like.of(user2, posts.get(3))); //delete
        likes.add(Like.of(user2, posts.get(4))); //delete
        likes.add(Like.of(user2, posts.get(5)));
        likes.add(Like.of(user2, posts.get(6)));
        likeRepository.saveAll(likes);

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.of(posts.get(0), user2, "content")); //delete
        comments.add(Comment.of(posts.get(1), user2, "content")); //delete
        comments.add(Comment.of(posts.get(1), user2, "content")); //delete
        comments.add(Comment.of(posts.get(1), user2, "content")); //delete
        comments.add(Comment.of(posts.get(2), user2, "content")); //delete
        comments.add(Comment.of(posts.get(3), user2, "content")); //delete
        comments.add(Comment.of(posts.get(4), user2, "content")); //delete
        comments.add(Comment.of(posts.get(5), user2, "content"));
        comments.add(Comment.of(posts.get(6), user2, "content"));
        comments.add(Comment.of(posts.get(7), user2, "content"));
        comments.add(Comment.of(posts.get(7), user2, "content"));
        commentRepository.saveAll(comments);

        em.flush();
        em.clear();

        //When
        postService.hardDeletePostsDeletedFor7Days();

        //Then
        long likeCount = (long) em.createNativeQuery("select count(l.id) from likes l").getSingleResult();
        long commentCount = (long) em.createNativeQuery("select count(c.id) from comments c").getSingleResult();
        long postCount = (long) em.createNativeQuery("select count(p.id) from posts p").getSingleResult();

        assertThat(likeCount).isEqualTo(2L);
        assertThat(commentCount).isEqualTo(4L);
        assertThat(postCount).isEqualTo(3L);
    }
}
