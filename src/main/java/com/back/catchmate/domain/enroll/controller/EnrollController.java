package com.back.catchmate.domain.enroll.controller;

import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.EnrollDescriptionInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.NewEnrollCountInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollRequestInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.UpdateEnrollInfo;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "직관 신청 관련 API")
@RestController
@RequestMapping("/enrolls")
@RequiredArgsConstructor
public class EnrollController {
    private final EnrollService enrollService;

    @PostMapping("/{boardId}")
    @Operation(summary = "직관 신청 API", description = "직관 신청을 요청하는 API 입니다.")
    public CreateEnrollInfo requestEnroll(@Valid @RequestBody CreateEnrollRequest createEnrollRequest,
                                          @PathVariable Long boardId,
                                          @JwtValidation Long userId) throws IOException {
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
                                                       @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                       @Parameter(hidden = true) Pageable pageable) {
        return enrollService.getReceiveEnrollList(userId, pageable);
    }

    @GetMapping("/receive")
    @Operation(summary = "내가 작성한 게시글에 대한 직관 신청 목록 조회 API", description = "내가 작성한 게시글에 대한 직관 신청 목록을 조회하는 API 입니다.")
    public PagedEnrollReceiveInfo getReceiveEnrollListByBoardId(@JwtValidation Long userId,
                                                                @RequestParam Long boardId,
                                                                @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                                @Parameter(hidden = true) Pageable pageable) {
        return enrollService.getReceiveEnrollListByBoardId(userId, boardId, pageable);
    }

    @GetMapping("/new-count")
    @Operation(summary = "내가 작성한 게시글에 대한 새로운 직관 신청 갯수 반환 API", description = "내가 작성한 게시글에 대한 새로윤 직관 신청 갯수를 반환하는 API 입니다.")
    public NewEnrollCountInfo getNewEnrollmentListCount(@JwtValidation Long userId) {
        return enrollService.getNewEnrollListCount(userId);
    }

    @PatchMapping("/{enrollId}/accept")
    @Operation(summary = "받은 직관 신청 수락 API", description = "내가 받은 직관 신청을 수락하는 API 입니다.")
    public UpdateEnrollInfo acceptEnroll(@PathVariable Long enrollId,
                                         @JwtValidation Long userId) throws IOException {
        return enrollService.acceptEnroll(enrollId, userId);
    }

    @PatchMapping("/{enrollId}/reject")
    @Operation(summary = "받은 직관 신청 거절 API", description = "내가 받은 직관 신청을 거절하는 API 입니다.")
    public UpdateEnrollInfo rejectEnroll(@PathVariable Long enrollId,
                                         @JwtValidation Long userId) throws IOException {
        return enrollService.rejectEnroll(enrollId, userId);
    }

    @GetMapping("/{enrollId}/description")
    @Operation(summary = "보낸 신청 상세 조회 API", description = "보낸 신청의 상세 내용을 조회하는 API 입니다.")
    public EnrollDescriptionInfo getEnrollDescriptionById(@PathVariable Long enrollId,
                                                          @JwtValidation Long userId) {
        return enrollService.getEnrollDescriptionById(enrollId, userId);
    }
}
