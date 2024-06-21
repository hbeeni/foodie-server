package com.been.foodieserver.repository.cache;

import com.been.foodieserver.domain.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CategoryCacheRepository {

    private final RedisTemplate<String, Category> redisTemplate;

    public void save(Category category) {
        String key = getKey(category.getId());
        redisTemplate.opsForValue().set(key, category);
    }

    public Category findById(Long categoryId) {
        String key = getKey(categoryId);
        return redisTemplate.opsForValue().get(key);
    }

    private String getKey(Long categoryId) {
        return "cat:" + categoryId;
    }
}
