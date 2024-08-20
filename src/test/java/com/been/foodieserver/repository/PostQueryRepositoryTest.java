package com.been.foodieserver.repository;

import com.been.foodieserver.config.JpaConfig;
import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.PostSearchDto;
import com.been.foodieserver.dto.PostSearchType;
import com.been.foodieserver.repository.cache.PostSearchCacheRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.then;

@Import({JpaConfig.class, PostQueryRepositoryTest.TestQueryDslConfig.class})
@DataJpaTest
class PostQueryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostQueryRepository postQueryRepository;

    @MockBean
    private PostSearchCacheRepository postSearchCacheRepository;

    private static final String TITLE_1 = "Hello World";
    private static final String TITLE_2 = "hello w";
    private static final String TITLE_3 = "Hello world?";
    private static final String TITLE_4 = "hello wor";
    private static final String TITLE_5 = "Hello worl";
    private static final String TITLE_6 = "WORLD";
    private static final String TITLE_7 = "worl";
    private static final String TITLE_8 = "llo world hello";
    private static final String TITLE_9 = "hhhhh";
    private static final String TITLE_10 = "test";

    @BeforeEach
    void setUp() {
        User user1 = User.of("writer1", "pwd", "writer1", null, Role.USER);
        User user2 = User.of("writer2", "pwd", "writer2", null, Role.USER);
        User user3 = User.of("user3", "pwd", "user3", null, Role.USER);
        userRepository.saveAllAndFlush(List.of(user1, user2, user3));

        Category category = Category.of("category", null);
        categoryRepository.saveAndFlush(category);

        Post post1 = Post.of(user1, category, TITLE_1, "content");
        Post post2 = Post.of(user1, category, TITLE_2, "content");
        Post post3 = Post.of(user1, category, TITLE_3, "content");
        Post post4 = Post.of(user2, category, TITLE_4, "content");
        Post post5 = Post.of(user2, category, TITLE_5, "content");
        Post post6 = Post.of(user2, category, TITLE_6, "content");
        Post post7 = Post.of(user2, category, TITLE_7, "content");
        Post post8 = Post.of(user3, category, TITLE_8, "content");
        Post post9 = Post.of(user3, category, TITLE_9, "content");
        Post post10 = Post.of(user3, category, TITLE_10, "content");
        postRepository.saveAllAndFlush(List.of(post1, post2, post3, post4, post5, post6, post7, post8, post9, post10));
    }

    @DisplayName("작성자 닉네임 기준 게시글 검색")
    @MethodSource
    @ParameterizedTest
    void findPostPageByWriterNickname(String nickname, List<String> expected) {
        //Given
        PostSearchDto dto = PostSearchDto.builder()
                .searchType(PostSearchType.WRITER_NICKNAME)
                .keyword(nickname)
                .pageNum(1)
                .pageSize(10)
                .build();

        //When
        Page<Post> result = postQueryRepository.findAllByUserNicknameContainsIgnoreCase(dto);
        List<String> actual = result.getContent().stream()
                .map(Post::getTitle)
                .toList();

        //Then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        then(postSearchCacheRepository).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 제목 기준 게시글 검색")
    @MethodSource
    @ParameterizedTest
    void findPostPageByTitle(String title, List<String> expected) {
        //Given
        PostSearchDto dto = PostSearchDto.builder()
                .searchType(PostSearchType.TITLE)
                .keyword(title)
                .pageNum(1)
                .pageSize(10)
                .build();

        //When
        Page<Post> result = postQueryRepository.findAllByTitleContainsIgnoreCase(dto);
        List<String> actual = result.getContent().stream()
                .map(Post::getTitle)
                .toList();

        //Then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        if (title != null && !title.isBlank()) {
            then(postSearchCacheRepository).should().incrementSearchKeywordCount(title);
        }
    }

    static Stream<Arguments> findPostPageByWriterNickname() {
        /*
        Post post1 = Post.of(user1, category, "Hello World", "content");
        Post post2 = Post.of(user1, category, "hello w", "content");
        Post post3 = Post.of(user1, category, "Hello world?", "content");
        Post post4 = Post.of(user2, category, "hello wor", "content");
        Post post5 = Post.of(user2, category, "Hello worl", "content");
        Post post6 = Post.of(user2, category, "WORLD", "content");
        Post post7 = Post.of(user2, category, "worl", "content");
        Post post8 = Post.of(user3, category, "llo world hello", "content");
        Post post9 = Post.of(user3, category, "hhhhh", "content");
        Post post10 = Post.of(user3, category, "test", "content");
         */
        return Stream.of(
                arguments(null, List.of()),
                arguments("", List.of()),
                arguments("writer", List.of(TITLE_1, TITLE_2, TITLE_3, TITLE_4, TITLE_5, TITLE_6, TITLE_7)),
                arguments("WRITER", List.of(TITLE_1, TITLE_2, TITLE_3, TITLE_4, TITLE_5, TITLE_6, TITLE_7)),
                arguments("writer1", List.of(TITLE_1, TITLE_2, TITLE_3)),
                arguments("writer2", List.of(TITLE_4, TITLE_5, TITLE_6, TITLE_7)),
                arguments("user", List.of(TITLE_8, TITLE_9, TITLE_10)),
                arguments("user3", List.of(TITLE_8, TITLE_9, TITLE_10))
        );
    }

    static Stream<Arguments> findPostPageByTitle() {
        /*
        Post post1 = Post.of(user1, category, "Hello World", "content");
        Post post2 = Post.of(user1, category, "hello w", "content");
        Post post3 = Post.of(user1, category, "Hello world?", "content");
        Post post4 = Post.of(user2, category, "hello wor", "content");
        Post post5 = Post.of(user2, category, "Hello worl", "content");
        Post post6 = Post.of(user2, category, "WORLD", "content");
        Post post7 = Post.of(user2, category, "worl", "content");
        Post post8 = Post.of(user3, category, "llo world hello", "content");
        Post post9 = Post.of(user3, category, "hhhhh", "content");
        Post post10 = Post.of(user3, category, "test", "content");
         */
        return Stream.of(
                arguments(null, List.of()),
                arguments("", List.of()),
                arguments("Hello World", List.of(TITLE_1, TITLE_3)),
                arguments("hello", List.of(TITLE_1, TITLE_2, TITLE_3, TITLE_4, TITLE_5, TITLE_8)),
                arguments("world", List.of(TITLE_1, TITLE_3, TITLE_6, TITLE_8)),
                arguments("llo world", List.of(TITLE_1, TITLE_3, TITLE_8)),
                arguments("test", List.of(TITLE_10)),
                arguments("hhh", List.of(TITLE_9)),
                arguments("worl", List.of(TITLE_1, TITLE_3, TITLE_5, TITLE_6, TITLE_7, TITLE_8)),
                arguments("something", List.of())
        );
    }

    @TestConfiguration
    static class TestQueryDslConfig {

        @Autowired
        private EntityManager entityManager;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(entityManager);
        }

        @Bean
        public PostQueryRepository postQueryRepository() {
            return new PostQueryRepository(jpaQueryFactory(), postSearchCacheRepository());
        }

        @Bean
        public PostSearchCacheRepository postSearchCacheRepository() {
            return new PostSearchCacheRepository(redisTemplate());
        }

        @Bean
        public RedisTemplate<String, String> redisTemplate() {
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory());
            return template;
        }

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory();
        }
    }
}
