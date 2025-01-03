package com.back.catchmate.domain.notification.dto;

import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public abstract class NotificationResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationInfo {
        private Long notificationId;
        private BoardInfo boardInfo;
        private String senderProfileImageUrl;
        private String title;
        private String body;
        private LocalDateTime createdAt;
        private boolean isRead;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedNotificationInfo {
        private List<NotificationInfo> notificationInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }
}
