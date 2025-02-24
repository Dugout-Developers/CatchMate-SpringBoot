package com.back.catchmate.domain.admin.notice.service;

import com.back.catchmate.domain.admin.notice.dto.NoticeRequest;
import com.back.catchmate.domain.admin.notice.dto.NoticeResponse;
import com.back.catchmate.global.dto.StateResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AdminNoticeService {
    NoticeResponse.NoticeInfo createNotice(Long userId, NoticeRequest.CreateNoticeRequest noticeRequest);

    NoticeResponse.NoticeInfo updateNotice(Long userId, Long noticeId, NoticeRequest.UpdateNoticeRequest request);

    StateResponse deleteNotice(Long userId, Long noticeId);

    NoticeResponse.NoticeInfo getNotice(Long noticeId);

    NoticeResponse.PagedNoticeInfo getNoticeList(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
