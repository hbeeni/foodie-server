package com.been.foodieserver.repository.cache;

import com.been.foodieserver.domain.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LikeCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void save(Like like) {
        String key = getKey(like.getPost().getId());
        redisTemplate.opsForSet().add(key, like.getUser().getLoginId());
    }

    public int countByPostId(Long postId) {
        String key = getKey(postId);
        Long likeCount = redisTemplate.opsForSet().size(key);
        return likeCount == null ? 0 : likeCount.intValue();
    }

    public void deleteByUserLoginIdAndPostId(String loginId, Long postId) {
        redisTemplate.opsForSet().remove(getKey(postId), loginId);
    }

    public boolean existsByUserLoginIdAndPostId(String loginId, Long postId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(getKey(postId), loginId));
    }

    private String getKey(Long postId) {
        return "post:like:" + postId;
    }
}
