package com.back.catchmate.domain.admin.notice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class NoticeRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateNoticeRequest {
        @NotNull
        private String title;
        @NotNull
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateNoticeRequest {
        @NotNull
        private String title;
        @NotNull
        private String content;
    }
}
