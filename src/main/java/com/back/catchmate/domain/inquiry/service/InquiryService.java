package com.back.catchmate.domain.inquiry.service;

import com.back.catchmate.domain.inquiry.dto.InquiryRequest;
import com.back.catchmate.global.dto.StateResponse;

public interface InquiryService {
    StateResponse submitInquiry(Long userId, InquiryRequest.CreateInquiryRequest request);
}
