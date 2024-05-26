package com.been.foodieserver.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    NEW_COMMENT_ON_POST("%s님이 게시글에 댓글을 달았습니다."),
    NEW_LIKE_ON_POST("%s님이 게시글에 좋아요를 눌렀습니다."),
    NEW_FOLLOW("%s님이 팔로우하였습니다.");

    private final String notificationText;
}
