package com.back.catchmate.domain.inquiry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryType {
    ACCOUNT("계정, 로그인 관련"),
    POST("게시글 관련"),
    CHAT("채팅 관련"),
    USER("유저 관련"),
    OTHER("기타");

    private final String description;
}
