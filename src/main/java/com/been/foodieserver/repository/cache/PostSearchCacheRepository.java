package com.been.foodieserver.repository.cache;

import com.been.foodieserver.dto.response.PostSearchRankResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Repository
public class PostSearchCacheRepository {

    private static final String POST_SEARCH_RANK_KEY = "post:search:rank";

    private final RedisTemplate<String, String> stringRedisTemplate;

    public List<PostSearchRankResponse> getSearchKeywordsRank(int start, int end) {
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().reverseRangeWithScores(POST_SEARCH_RANK_KEY, start, end);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return new ArrayList<>();
        }
        return typedTuples.stream().map(tuple -> PostSearchRankResponse.of(tuple.getValue(), tuple.getScore())).toList();
    }

    public void incrementSearchKeywordCount(String keyword) {
        keyword = keyword.trim();
        stringRedisTemplate.opsForZSet().incrementScore(POST_SEARCH_RANK_KEY, keyword, 1.0);
    }
}
