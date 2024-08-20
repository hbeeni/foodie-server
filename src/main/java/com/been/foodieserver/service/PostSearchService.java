package com.been.foodieserver.service;

import com.been.foodieserver.dto.PostSearchDto;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.dto.response.PostSearchRankResponse;
import com.been.foodieserver.repository.PostQueryRepository;
import com.been.foodieserver.repository.cache.PostSearchCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostSearchService {

    private final PostQueryRepository postQueryRepository;
    private final PostSearchCacheRepository postSearchCacheRepository;

    @Transactional(readOnly = true)
    public Page<PostResponse> search(PostSearchDto dto) {
        return switch (dto.getSearchType()) {
            case WRITER_NICKNAME -> postQueryRepository.findAllByUserNicknameContainsIgnoreCase(dto).map(PostResponse::of);
            case TITLE -> postQueryRepository.findAllByTitleContainsIgnoreCase(dto).map(PostResponse::of);
        };
    }

    @Transactional(readOnly = true)
    public List<PostSearchRankResponse> getTop10SearchKeywords() {
        return postSearchCacheRepository.getSearchKeywordsRank(0, 9);
    }
}
