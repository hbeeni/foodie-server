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

    public Page<Post> findAllByUserNicknameContainsIgnoreCase(PostSearchDto dto) {
        if (dto.getKeyword() == null || dto.getKeyword().isBlank()) {
            return Page.empty();
        }

        Pageable pageable = getPageable(dto);

        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.category, category).fetchJoin()
                .join(post.user, user).fetchJoin()
                .where(writerNicknameContainsIgnoreCase(dto.getKeyword()))
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .leftJoin(post.category, category)
                .leftJoin(post.user, user)
                .where(writerNicknameContainsIgnoreCase(dto.getKeyword()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Page<Post> findAllByTitleContainsIgnoreCase(PostSearchDto dto) {
        if (dto.getKeyword() == null || dto.getKeyword().isBlank()) {
            return Page.empty();
        }

        postSearchCacheRepository.incrementSearchKeywordCount(dto.getKeyword());

        Pageable pageable = getPageable(dto);

        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.category, category).fetchJoin()
                .join(post.user, user).fetchJoin()
                .where(postTitleContainsIgnoreCase(dto.getKeyword()))
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .leftJoin(post.category, category)
                .leftJoin(post.user, user)
                .where(postTitleContainsIgnoreCase(dto.getKeyword()));
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression writerNicknameContainsIgnoreCase(String nickname) {
        return StringUtils.hasText(nickname) ? user.nickname.containsIgnoreCase(nickname.trim()) : null;
    }

    private BooleanExpression postTitleContainsIgnoreCase(String title) {
        return StringUtils.hasText(title) ? post.title.containsIgnoreCase(title.trim()) : null;
    }

    private static PageRequest getPageable(PostSearchDto dto) {
        return PageRequest.of(dto.getPageNum() - 1, dto.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
    }
}
