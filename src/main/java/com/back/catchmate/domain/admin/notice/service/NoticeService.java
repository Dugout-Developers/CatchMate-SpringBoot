package com.back.catchmate.domain.admin.notice.service;

import com.back.catchmate.domain.admin.notice.dto.NoticeRequest;
import com.back.catchmate.domain.admin.notice.dto.NoticeResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface NoticeService {
    NoticeResponse.NoticeInfo create(Long userId, NoticeRequest.CreateNoticeRequest noticeRequest);

    NoticeResponse.NoticeInfo update(Long userId, Long noticeId, NoticeRequest.UpdateNoticeRequest request);

    void delete(Long userId, Long noticeId);

    NoticeResponse.NoticeInfo getNotice(Long noticeId);

    NoticeResponse.PagedNoticeInfo getNoticeList(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
