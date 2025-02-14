package com.back.catchmate.domain.report.dto;

import com.back.catchmate.domain.report.entity.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class ReportRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReportRequest {
        @NotNull
        private Long reportedUserId;
        @NotNull
        private ReportReason reportReason;
        @NotNull
        private String reasonDetail;
    }
}
