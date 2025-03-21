package com.back.catchmate.domain.notice.dto;

import com.back.catchmate.domain.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public abstract class NoticeResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeInfo {
        private Long noticeId;
        private String title;
        private String content;
        private UserResponse.UserInfo userInfo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PagedNoticeInfo {
        private List<NoticeInfo> noticeInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }
}
