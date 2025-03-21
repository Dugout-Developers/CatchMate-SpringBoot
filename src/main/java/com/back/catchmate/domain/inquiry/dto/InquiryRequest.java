package com.back.catchmate.domain.inquiry.dto;

import com.back.catchmate.domain.inquiry.entity.InquiryType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class InquiryRequest {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateInquiryRequest {
        @NotNull
        private InquiryType inquiryType;
        @NotNull
        private String content;
    }
}
