package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Post;
import com.been.foodieserver.dto.PostSearchDto;
import com.been.foodieserver.repository.cache.PostSearchCacheRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.been.foodieserver.domain.QCategory.category;
import static com.been.foodieserver.domain.QPost.post;
import static com.been.foodieserver.domain.QUser.user;

@RequiredArgsConstructor
@Repository
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PostSearchCacheRepository postSearchCacheRepository;

    public Page<Post> findAllByUserLoginIdContainsIgnoreCaseAndTitleContainsIgnoreCase(PostSearchDto dto) {
        if (dto.getTitle() != null) {
            postSearchCacheRepository.incrementSearchKeywordCount(dto.getTitle());
        }

        Pageable pageable = PageRequest.of(dto.getPageNum() - 1, dto.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));

        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.category, category).fetchJoin()
                .join(post.user, user).fetchJoin()
                .where(
                        writerLoginIdContainsIgnoreCase(dto.getWriterLoginId()),
                        postTitleContainsIgnoreCase(dto.getTitle())
                )
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .leftJoin(post.category, category)
                .leftJoin(post.user, user)
                .where(
                        writerLoginIdContainsIgnoreCase(dto.getWriterLoginId()),
                        postTitleContainsIgnoreCase(dto.getTitle())
                );
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression writerLoginIdContainsIgnoreCase(String writerLoginId) {
        return StringUtils.hasText(writerLoginId) ? user.loginId.containsIgnoreCase(writerLoginId.trim()) : null;
    }

    private BooleanExpression postTitleContainsIgnoreCase(String title) {
        return StringUtils.hasText(title) ? post.title.containsIgnoreCase(title.trim()) : null;
    }
}
