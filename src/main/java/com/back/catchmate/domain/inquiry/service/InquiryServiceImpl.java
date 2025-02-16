package com.back.catchmate.domain.inquiry.service;

import com.back.catchmate.domain.inquiry.converter.InquiryConverter;
import com.back.catchmate.domain.inquiry.dto.InquiryRequest.CreateInquiryRequest;
import com.back.catchmate.domain.inquiry.entity.Inquiry;
import com.back.catchmate.domain.inquiry.repository.InquiryRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final InquiryConverter inquiryConverter;

    @Override
    @Transactional
    public StateResponse submitInquiry(Long userId, CreateInquiryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Inquiry inquiry = inquiryConverter.toEntity(user, request);
        inquiryRepository.save(inquiry);
        return new StateResponse(true);
    }
}
