package com.back.catchmate.domain.notice.service;

import com.back.catchmate.domain.notice.dto.NoticeResponse;
import org.springframework.data.domain.Pageable;

public interface NoticeService {
    NoticeResponse.PagedNoticeInfo getNoticeList(Pageable pageable);

    NoticeResponse.NoticeInfo getNotice(Long noticeId);
}
