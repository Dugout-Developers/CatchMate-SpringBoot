package com.back.catchmate.domain.enroll.controller;

import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollRequestInfo;
import com.back.catchmate.domain.enroll.service.EnrollService;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "직관 신청 관련 API")
@RestController
@RequestMapping("/enroll")
@RequiredArgsConstructor
public class EnrollController {
    private final EnrollService enrollService;

    @PostMapping("/{boardId}")
    @Operation(summary = "직관 신청 API", description = "직관 신청을 요청하는 API 입니다.")
    public CreateEnrollInfo requestEnroll(@Valid @RequestBody CreateEnrollRequest createEnrollRequest,
                                          @PathVariable Long boardId,
                                          @JwtValidation Long userId) {
        return enrollService.requestEnroll(createEnrollRequest, boardId, userId);
    }

    @DeleteMapping("/cancel/{enrollId}")
    @Operation(summary = "직관 신청 취소 API", description = "직관 신청을 취소하는 API 입니다.")
    public CancelEnrollInfo cancelEnroll(@PathVariable Long enrollId,
                                         @JwtValidation Long userId) {
        return enrollService.cancelEnroll(enrollId, userId);
    }

    @GetMapping("/request")
    @Operation(summary = "내가 보낸 직관 신청 목록 조회 API", description = "내가 보낸 직관 신청 목록을 조회하는 API 입니다.")
    public PagedEnrollRequestInfo getRequestEnrollList(@JwtValidation Long userId,
                                                       @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                       @Parameter(hidden = true) Pageable pageable) {
        return enrollService.getRequestEnrollList(userId, pageable);
    }

    @GetMapping("/receive/all")
    @Operation(summary = "내가 작성한 게시글에 대한 직관 신청 목록 전체 조회 API", description = "내가 작성한 게시글에 대한 직관 신청 목록을 전체 조회하는 API 입니다.")
    public PagedEnrollReceiveInfo getReceiveEnrollList(@JwtValidation Long userId,
                                                       @Parameter(hidden = true) Pageable pageable) {
        return enrollService.getReceiveEnrollList(userId, pageable);
    }

    @GetMapping("/receive")
    @Operation(summary = "내가 작성한 게시글에 대한 직관 신청 목록 조회 API", description = "내가 작성한 게시글에 대한 직관 신청 목록을 조회하는 API 입니다.")
    public PagedEnrollReceiveInfo getReceiveEnrollListByBoardId(@JwtValidation Long userId,
                                                                @RequestParam Long boardId,
                                                                @Parameter(hidden = true) Pageable pageable) {
        return enrollService.getReceiveEnrollListByBoardId(userId, boardId, pageable);
    }
}
