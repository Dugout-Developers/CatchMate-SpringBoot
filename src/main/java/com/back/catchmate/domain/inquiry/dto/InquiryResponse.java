package com.back.catchmate.domain.inquiry.dto;

import com.back.catchmate.domain.inquiry.entity.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public abstract class InquiryResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquiryInfo {
        private Long inquiryId;
        private InquiryType inquiryType;
        private String content;
        private String nickName;
        private String answer;
        private Boolean isCompleted;
        private LocalDateTime createdAt;
    }
}
