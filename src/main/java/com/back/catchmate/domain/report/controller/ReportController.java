package com.back.catchmate.domain.report.controller;

import com.back.catchmate.domain.report.dto.ReportRequest.CreateReportRequest;
import com.back.catchmate.domain.report.service.ReportService;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[사용자] 신고 관련 API")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/{reportedUserId}")
    @Operation(summary = "유저 신고 API", description = """ 
                    유저를 신고하는 API입니다. 신고 사유 목록은 다음과 같습니다:
                    - **PROFANITY**: 욕설 / 비하 발언
                    
                    - **DEFAMATION**: 선수 혹은 특정인 비방
                    
                    - **PRIVACY_INVASION**: 개인 사생활 침해
                    
                    - **SPAM**: 게시글 도배
                    
                    - **ADVERTISEMENT**: 홍보성 게시글
                    
                    - **FALSE_INFORMATION**: 허위사실 유포
                    
                    - **OTHER**: 기타
                    """
    )
    public StateResponse reportUser(@JwtValidation Long userId,
                                    @PathVariable Long reportedUserId,
                                    @Valid @RequestBody CreateReportRequest request) {
        return reportService.reportUser(userId, reportedUserId, request);
    }
}
