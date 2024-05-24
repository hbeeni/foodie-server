package com.been.foodieserver.service;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.repository.CategoryRepository;
import com.been.foodieserver.repository.PostRepository;
import com.been.foodieserver.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(properties = {"schedules.cron.post.delete=0/2 * * * * *"})
class PostServiceSchedulerTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.of("user", "pwd", "nick1", Role.USER);
        userRepository.saveAndFlush(user);

        category = Category.of("category", null);
        categoryRepository.saveAndFlush(category);
    }

    @DisplayName("7일 전에 삭제된 게시글을 DB에서 삭제")
    @Test
    void schedule_DeletePostsDeleted7DaysAgoFromDB() {
        //Given
        List<Post> posts = new ArrayList<>();
        posts.add(Post.of(user, category, "title", "content"));
        posts.add(Post.of(user, category, "title", "content"));
        posts.add(Post.of(user, category, "title", "content"));
        posts.add(Post.of(user, category, "title", "content")); //delete
        posts.add(Post.of(user, category, "title", "content")); //delete
        posts.add(Post.of(user, category, "title", "content"));

        ReflectionTestUtils.setField(posts.get(0), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        ReflectionTestUtils.setField(posts.get(1), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(2)));
        ReflectionTestUtils.setField(posts.get(2), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(3)));
        ReflectionTestUtils.setField(posts.get(3), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(7)));
        ReflectionTestUtils.setField(posts.get(4), "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(8)));

        postRepository.saveAllAndFlush(posts);

        //When & Then
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    long postCount = (long) em.createNativeQuery("select count(p.id) from posts p").getSingleResult();
                    System.out.println("post: " + postCount);
                    assertThat(postCount).isEqualTo(4L);
                });
    }
}
