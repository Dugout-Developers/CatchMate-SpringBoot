package com.back.catchmate.domain.inquiry.converter;

import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.inquiry.dto.InquiryRequest.CreateInquiryRequest;
import com.back.catchmate.domain.inquiry.dto.InquiryResponse;
import com.back.catchmate.domain.inquiry.entity.Inquiry;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class InquiryConverter {
    public Inquiry toEntity(User user, CreateInquiryRequest request) {
        return Inquiry.builder()
                .user(user)
                .inquiryType(request.getInquiryType())
                .content(request.getContent())
                .isCompleted(false)
                .build();
    }

    public InquiryResponse.InquiryInfo toInquiryInfo(Inquiry inquiry) {
        return InquiryResponse.InquiryInfo.builder()
                .inquiryId(inquiry.getId())
                .inquiryType(inquiry.getInquiryType())
                .content(inquiry.getContent())
                .nickName(inquiry.getUser().getNickName())
                .answer(inquiry.getAnswer())
                .isCompleted(inquiry.getIsCompleted())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
