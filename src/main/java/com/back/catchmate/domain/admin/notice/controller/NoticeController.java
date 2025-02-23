package com.back.catchmate.domain.admin.notice.controller;

import com.back.catchmate.domain.admin.notice.dto.NoticeRequest;
import com.back.catchmate.domain.admin.notice.dto.NoticeResponse;
import com.back.catchmate.domain.admin.notice.service.NoticeService;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "[관리자] 공지사항 관련 API")
@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @PostMapping
    @Operation(summary = "공지글 등록 API", description = "공지글을 등록하는 API 입니다.")
    public NoticeResponse.NoticeInfo createNotice(@JwtValidation Long userId,
                                                  @Valid @RequestBody NoticeRequest.CreateNoticeRequest request) {
        return noticeService.createNotice(userId, request);
    }

    @GetMapping("/list")
    @Operation(summary = "공지사항 목록 조회 API", description = "공지사항 목록을 페이징하여 조회하는 API 입니다.")
    public NoticeResponse.PagedNoticeInfo getNoticeList(@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate startDate,
                                                        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate endDate,
                                                        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                        @Parameter(hidden = true) Pageable pageable) {
        return noticeService.getNoticeList(startDate, endDate, pageable);
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 단일 조회 API", description = "특정 공지사항을 조회하는 API 입니다.")
    public NoticeResponse.NoticeInfo getNotice(@PathVariable Long noticeId) {
        return noticeService.getNotice(noticeId);
    }

    @PutMapping("/{noticeId}")
    @Operation(summary = "공지사항 수정 API", description = "공지사항을 수정하는 API 입니다.")
    public NoticeResponse.NoticeInfo updateNotice(@JwtValidation Long userId,
                                                  @PathVariable Long noticeId,
                                                  @Valid @RequestBody NoticeRequest.UpdateNoticeRequest request) {
        return noticeService.updateNotice(userId, noticeId, request);
    }

    @DeleteMapping("/{noticeId}")
    @Operation(summary = "공지사항 삭제 API", description = "공지사항을 삭제하는 API 입니다.")
    public StateResponse deleteNotice(@JwtValidation Long userId,
                                      @PathVariable Long noticeId) {
        return noticeService.deleteNotice(userId, noticeId);
    }
}
