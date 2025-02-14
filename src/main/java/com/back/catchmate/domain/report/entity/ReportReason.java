package com.back.catchmate.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
    PROFANITY("욕설 / 비하 발언"),
    DEFAMATION("선수 혹은 특정인 비방"),
    PRIVACY_INVASION("개인 사생활 침해"),
    SPAM("게시글 도배"),
    ADVERTISEMENT("홍보성 게시글"),
    FALSE_INFORMATION("허위사실 유포"),
    OTHER("기타");

    private final String description;
}
