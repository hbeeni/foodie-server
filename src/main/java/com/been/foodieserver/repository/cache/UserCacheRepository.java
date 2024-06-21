package com.been.foodieserver.repository.cache;

import com.been.foodieserver.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserCacheRepository {

    private final RedisTemplate<String, User> redisTemplate;

    public void save(User user) {
        String key = getKey(user.getLoginId());
        redisTemplate.opsForValue().set(key, user);
    }

    public Map<String, User> getLoginIdMap(Collection<String> userLoginIds) {
        List<User> users = userLoginIds.stream().map(this::findByLoginId).map(user -> user.orElse(null)).toList();
        return users.stream().collect(Collectors.toMap(User::getLoginId, Function.identity(), (oldV, newV) -> oldV));
    }

    public Optional<User> findByLoginId(String loginId) {
        String key = getKey(loginId);
        User user = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(user);
    }

    public void modify(User user) {
        deleteByLoginId(user.getLoginId());
        save(user);
    }

    public boolean existsByLoginId(String loginId) {
        Long size = redisTemplate.opsForValue().size(getKey(loginId));
        return size != null && !Objects.equals(size, 0L);
    }

    public void deleteByLoginId(String loginId) {
        String key = getKey(loginId);
        redisTemplate.delete(key);
    }

    private String getKey(String loginId) {
        return "user:" + loginId;
    }
}
