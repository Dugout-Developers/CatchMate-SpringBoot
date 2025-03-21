package com.back.catchmate.domain.notice.controller;

import com.back.catchmate.domain.notice.dto.NoticeResponse;
import com.back.catchmate.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[사용자] 공지사항 관련 API")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping("/list")
    @Operation(summary = "공지사항 목록 조회 API", description = "공지사항 목록을 페이징하여 조회하는 API 입니다.")
    public NoticeResponse.PagedNoticeInfo getNoticeList(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                        @Parameter(hidden = true) Pageable pageable) {
        return noticeService.getNoticeList(pageable);
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 단일 조회 API", description = "특정 공지사항을 조회하는 API 입니다.")
    public NoticeResponse.NoticeInfo getNotice(@PathVariable Long noticeId) {
        return noticeService.getNotice(noticeId);
    }
}
