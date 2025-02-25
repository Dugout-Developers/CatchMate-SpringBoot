package com.back.catchmate.domain.notice.service;

import com.back.catchmate.domain.notice.converter.NoticeConverter;
import com.back.catchmate.domain.notice.dto.NoticeResponse;
import com.back.catchmate.domain.notice.entity.Notice;
import com.back.catchmate.domain.notice.repository.NoticeRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeConverter noticeConverter;

    @Override
    @Transactional(readOnly = true)
    public com.back.catchmate.domain.notice.dto.NoticeResponse.PagedNoticeInfo getNoticeList(Pageable pageable) {
        Page<Notice> noticeList = noticeRepository.findAllByDeletedAtIsNull(pageable);
        return noticeConverter.toPagedNoticeInfo(noticeList);
    }

    @Override
    @Transactional
    public NoticeResponse.NoticeInfo getNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

        return noticeConverter.toNoticeInfo(notice);
    }
}
