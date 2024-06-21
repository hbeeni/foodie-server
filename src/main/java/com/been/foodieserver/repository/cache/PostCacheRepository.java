package com.been.foodieserver.repository.cache;

import com.been.foodieserver.domain.Category;
import com.been.foodieserver.domain.Post;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.domain.redis.RedisPost;
import com.been.foodieserver.dto.PageDto;
import com.been.foodieserver.dto.response.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Repository
public class PostCacheRepository {

    private static final String POST_ALL_KEY = "post:all";

    private final RedisTemplate<String, RedisPost> redisPostRedisTemplate;
    private final RedisTemplate<String, String> allPostStringRedisTemplate;

    private final UserCacheRepository userCacheRepository;
    private final CategoryCacheRepository categoryCacheRepository;
    private final LikeCacheRepository likeCacheRepository;
    private final CommentCacheRepository commentCacheRepository;

    public void save(Post post) {
        long score = post.getCreatedAt().getTime();
        RedisPost redisPost = RedisPost.of(post);

        allPostStringRedisTemplate.opsForZSet().add(POST_ALL_KEY, String.valueOf(post.getId()), score);
        redisPostRedisTemplate.opsForValue().set(getKey(post.getId()), redisPost);
    }

    public PageDto<PostResponse> findAll(int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;
        Set<String> postIds = allPostStringRedisTemplate.opsForZSet().reverseRange(POST_ALL_KEY, start, end);

        if (ObjectUtils.isEmpty(postIds)) {
            return PageDto.of(pageNum, pageSize, count(), List.of());
        }

        List<String> keys = postIds.stream().map(Long::valueOf).map(this::getKey).toList();
        List<RedisPost> posts = redisPostRedisTemplate.opsForValue().multiGet(keys);

        if (ObjectUtils.isEmpty(posts)) {
            return PageDto.of(pageNum, pageSize, count(), List.of());
        }

        Category category = categoryCacheRepository.findById(1L);

        List<String> userLoginIdList = posts.stream().map(RedisPost::getUserLoginId).toList();
        Map<String, User> userLoginIdMap = userCacheRepository.getLoginIdMap(userLoginIdList);

        List<PostResponse> content = posts.stream()
                .map(post -> {
                    int likeCount = likeCacheRepository.countByPostId(post.getId());
                    int commentCount = commentCacheRepository.countByPostId(post.getId());
                    return PostResponse.of(userLoginIdMap.get(post.getUserLoginId()), category, post, likeCount, commentCount);
                })
                .toList();

        return PageDto.of(pageNum, pageSize, count(), content);
    }

    public void modify(Post post) {
        deleteById(post.getId());
        save(post);
    }

    public void deleteById(Long postId) {
        String key = getKey(postId);
        allPostStringRedisTemplate.delete(key);
        redisPostRedisTemplate.opsForZSet().remove(key, String.valueOf(postId));
    }

    private long count() {
        Long postCount = redisPostRedisTemplate.opsForZSet().zCard(POST_ALL_KEY);
        return postCount == null ? 0L : postCount;
    }

    private String getKey(Long postId) {
        return "post:" + postId;
    }
}
