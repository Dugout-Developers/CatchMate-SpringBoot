package com.back.catchmate.domain.notification.dto;

import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.inquiry.dto.InquiryResponse;
import com.back.catchmate.domain.inquiry.dto.InquiryResponse.InquiryInfo;
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
        private InquiryInfo inquiryInfo;
        private String senderProfileImageUrl;
        private String title;
        private String body;
        private LocalDateTime createdAt;
        private boolean isRead;
        private AcceptStatus acceptStatus;
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
