package com.back.catchmate.domain.inquiry.converter;

import com.back.catchmate.domain.inquiry.dto.InquiryRequest.CreateInquiryRequest;
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
                .build();
    }
}
