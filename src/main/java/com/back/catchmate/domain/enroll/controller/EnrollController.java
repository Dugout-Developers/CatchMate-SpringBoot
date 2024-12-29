package com.back.catchmate.domain.enroll.controller;

import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.service.EnrollService;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "직관 신청 관련 API")
@RestController
@RequestMapping("/enroll")
@RequiredArgsConstructor
public class EnrollController {
    private final EnrollService enrollService;

    @PostMapping("/{boardId}")
    @Operation(summary = "직관 신청 API", description = "직관 신청을 요청하는 API 입니다.")
    public CreateEnrollInfo createEnroll(@Valid @RequestBody CreateEnrollRequest createEnrollRequest,
                                         @PathVariable Long boardId,
                                         @JwtValidation Long userId) throws IOException {
        return enrollService.createEnroll(createEnrollRequest, boardId, userId);
    }

    @DeleteMapping("/cancel/{enrollId}")
    @Operation(summary = "직관 신청 취소 API", description = "직관 신청을 취소하는 API 입니다.")
    public CancelEnrollInfo cancelEnroll(@PathVariable Long enrollId,
                                         @JwtValidation Long userId) {
        return enrollService.cancelEnroll(enrollId, userId);
    }
}
