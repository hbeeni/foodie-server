package com.been.foodieserver.service;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Comment;
import com.been.foodieserver.domain.Follow;
import com.been.foodieserver.domain.Like;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.repository.CategoryRepository;
import com.been.foodieserver.repository.CommentRepository;
import com.been.foodieserver.repository.FollowRepository;
import com.been.foodieserver.repository.LikeRepository;
import com.been.foodieserver.repository.PostRepository;
import com.been.foodieserver.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserServiceIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserService userService;

    @DisplayName("30일 전에 탈퇴한 회원 및 회원과 연관된 데이터를 DB에서 삭제")
    @Transactional
    @Test
    void hardDeleteUsersWithdrawn30DaysAgoAndRelatedData() {
        //Given
        User user1 = User.of("user1", "pwd", "nick1", Role.USER);
        User user2 = User.of("user2", "pwd", "nick2", Role.USER); //delete
        User user3 = User.of("user3", "pwd", "nick3", Role.USER); //delete
        User user4 = User.of("user4", "pwd", "nick4", Role.USER); //delete
        User user5 = User.of("user5", "pwd", "nick5", Role.USER);

        ReflectionTestUtils.setField(user1, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(29)));
        ReflectionTestUtils.setField(user2, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(30)));
        ReflectionTestUtils.setField(user3, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(31)));
        ReflectionTestUtils.setField(user4, "deletedAt", Timestamp.valueOf(LocalDateTime.now().minusDays(32)));

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5));

        Category category = Category.of("category", null);
        categoryRepository.saveAndFlush(category);

        List<Follow> follows = new ArrayList<>();
        follows.add(Follow.of(user1, user2)); //delete
        follows.add(Follow.of(user1, user4)); //delete
        follows.add(Follow.of(user2, user3)); //delete
        follows.add(Follow.of(user3, user4)); //delete
        follows.add(Follow.of(user1, user5));
        follows.add(Follow.of(user5, user1));
        follows.add(Follow.of(user5, user3)); //delete
        followRepository.saveAll(follows);

        Post post1 = Post.of(user1, category, "title", "content");
        Post post2 = Post.of(user2, category, "title", "content"); //delete
        Post post3 = Post.of(user3, category, "title", "content"); //delete
        Post post4 = Post.of(user4, category, "title", "content"); //delete
        Post post5 = Post.of(user1, category, "title", "content");
        Post post6 = Post.of(user4, category, "title", "content"); //delete
        Post post7 = Post.of(user1, category, "title", "content");
        Post post8 = Post.of(user5, category, "title", "content");
        postRepository.saveAllAndFlush(List.of(post1, post2, post3, post4, post5, post6, post7, post8));

        List<Like> likes = new ArrayList<>();
        likes.add(Like.of(user1, post2)); //delete
        likes.add(Like.of(user1, post3)); //delete
        likes.add(Like.of(user1, post4)); //delete
        likes.add(Like.of(user2, post1)); //delete
        likes.add(Like.of(user3, post6)); //delete
        likes.add(Like.of(user4, post5)); //delete
        likes.add(Like.of(user5, post5));
        likeRepository.saveAll(likes);

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.of(post1, user1, "content"));
        comments.add(Comment.of(post2, user1, "content")); //delete
        comments.add(Comment.of(post3, user1, "content")); //delete
        comments.add(Comment.of(post4, user1, "content")); //delete
        comments.add(Comment.of(post5, user1, "content"));
        comments.add(Comment.of(post6, user1, "content")); //delete
        comments.add(Comment.of(post5, user2, "content")); //delete
        comments.add(Comment.of(post1, user3, "content")); //delete
        comments.add(Comment.of(post1, user4, "content")); //delete
        comments.add(Comment.of(post1, user5, "content"));
        commentRepository.saveAll(comments);

        em.flush();
        em.clear();

        //When
        userService.deleteUsersInactiveFor30Days();

        //Then
        long likeCount = (long) em.createNativeQuery("select count(l.id) from likes l").getSingleResult();
        long commentCount = (long) em.createNativeQuery("select count(c.id) from comments c").getSingleResult();
        long postCount = (long) em.createNativeQuery("select count(p.id) from posts p").getSingleResult();
        long followCount = (long) em.createNativeQuery("select count(f.id) from follows f").getSingleResult();
        long userCount = (long) em.createNativeQuery("select count(u.id) from users u").getSingleResult();

        assertThat(likeCount).isOne();
        assertThat(commentCount).isEqualTo(3L);
        assertThat(postCount).isEqualTo(4L);
        assertThat(followCount).isEqualTo(2L);
        assertThat(userCount).isEqualTo(2L);
    }
}
