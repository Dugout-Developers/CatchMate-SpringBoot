package com.back.catchmate.domain.admin.notice.dto;

import com.back.catchmate.domain.admin.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NoticeResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeInfo {
        private Long noticeId;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static NoticeInfo from(Notice notice) {
            return NoticeInfo.builder()
                    .noticeId(notice.getId())
                    .title(notice.getTitle())
                    .content(notice.getContent())
                    .createdAt(notice.getCreatedAt())
                    .updatedAt(notice.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PagedNoticeInfo {
        private List<NoticeInfo> notices;
        private int totalPages;
        private long totalElements;
        private int currentPage;
        private int size;

        public static PagedNoticeInfo from(Page<Notice> noticePage) {
            return PagedNoticeInfo.builder()
                    .notices(noticePage.getContent().stream()
                            .map(NoticeInfo::from)
                            .collect(Collectors.toList()))
                    .totalPages(noticePage.getTotalPages())
                    .totalElements(noticePage.getTotalElements())
                    .currentPage(noticePage.getNumber())
                    .size(noticePage.getSize())
                    .build();
        }
    }
}