package com.back.catchmate.domain.admin.dto;

import com.back.catchmate.domain.report.entity.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class AdminRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerInquiryRequest {
        @NotNull
        private String answer;
    }
}
