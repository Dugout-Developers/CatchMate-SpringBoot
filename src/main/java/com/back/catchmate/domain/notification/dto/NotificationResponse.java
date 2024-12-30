package com.back.catchmate.domain.notification.dto;

import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public abstract class NotificationResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationInfo {
        private Long notificationId;
        private BoardInfo boardInfo;
        private String title;
        private String body;
        private LocalDateTime createdAt;
        private boolean isRead;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateNotificationInfo {
        private Long notificationId;
        private LocalDateTime createdAt;
    }
}
