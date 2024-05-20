package com.been.foodieserver.service;

import com.been.foodieserver.dto.PostSearchDto;
import com.been.foodieserver.dto.response.PostResponse;
import com.been.foodieserver.repository.PostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostSearchService {

    private final PostQueryRepository postQueryRepository;

    public Page<PostResponse> search(PostSearchDto dto) {
        return postQueryRepository.findAllByUserLoginIdContainsIgnoreCaseAndTitleContainsIgnoreCase(dto).map(PostResponse::of);
    }
}