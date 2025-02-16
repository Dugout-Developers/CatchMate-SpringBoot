package com.back.catchmate.domain.inquiry.controller;

import com.back.catchmate.domain.inquiry.dto.InquiryRequest.CreateInquiryRequest;
import com.back.catchmate.domain.inquiry.service.InquiryService;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "고객센터 관련 API")
@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;

    @PostMapping
    @Operation(summary = "고객센터 문의 API", description = """
                    고객센터에 문의를 등록하는 API입니다.
                    문의 유형 목록:
                    - **ACCOUNT**: 계정, 로그인 관련
                    
                    - **POST**: 게시글 관련
                    
                    - **CHAT**: 채팅 관련
                    
                    - **USER**: 유저 관련
                    
                    - **OTHER**: 기타
                    """
    )
    public StateResponse submitInquiry(@JwtValidation Long userId,
                                       @Valid @RequestBody CreateInquiryRequest request) {
        return inquiryService.submitInquiry(userId, request);
    }
}
