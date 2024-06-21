package com.been.foodieserver.repository.cache;

import com.been.foodieserver.domain.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CommentCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveId(Comment comment) {
        String key = getKey(comment.getPost().getId());
        redisTemplate.opsForSet().add(key, String.valueOf(comment.getId()));
    }

    public int countByPostId(Long postId) {
        String key = getKey(postId);
        Long commentCount = redisTemplate.opsForSet().size(key);
        return commentCount == null ? 0 : commentCount.intValue();
    }

    public void deleteByPostIdAndId(Long postId, Long commentId) {
        redisTemplate.opsForSet().remove(getKey(postId), commentId);
    }

    private String getKey(Long postId) {
        return "post:comment:" + postId;
    }
}
