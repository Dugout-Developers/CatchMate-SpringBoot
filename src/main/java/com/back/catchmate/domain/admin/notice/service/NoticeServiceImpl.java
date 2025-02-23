package com.back.catchmate.domain.admin.notice.service;

import com.back.catchmate.domain.admin.notice.converter.NoticeConverter;
import com.back.catchmate.domain.admin.notice.dto.NoticeRequest;
import com.back.catchmate.domain.admin.notice.dto.NoticeResponse;
import com.back.catchmate.domain.admin.notice.entity.Notice;
import com.back.catchmate.domain.admin.notice.repository.NoticeRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final UserRepository userRepository;
    private final NoticeConverter noticeConverter;
    private final NoticeRepository noticeRepository;

    @Override
    public NoticeResponse.NoticeInfo create(Long userId, NoticeRequest.CreateNoticeRequest noticeRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Notice notice = noticeConverter.toEntity(user, noticeRequest);
        notice = noticeRepository.save(notice);

        return noticeConverter.toNoticeInfo(notice);
    }

    @Override
    public NoticeResponse.NoticeInfo update(Long userId, Long noticeId, NoticeRequest.UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

//        if (!notice.getUser().getId().equals(userId)) {
//            throw new BaseException(ErrorCode.FORBIDDEN);
//        }

        Notice updatedNotice = noticeConverter.toUpdatedEntity(notice, request);
        noticeRepository.save(updatedNotice);

        return noticeConverter.toNoticeInfo(updatedNotice);
    }

    @Override
    public void delete(Long userId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

//        if (!notice.getUser().getId().equals(userId)) {
//            throw new BaseException(ErrorCode.FORBIDDEN);
//        }

        noticeRepository.delete(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponse.NoticeInfo getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

        return noticeConverter.toNoticeInfo(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponse.PagedNoticeInfo getNoticeList(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        Page<Notice> notices = noticeRepository.findNoticesWithinDateRange(startDateTime, endDateTime, pageable);
        return noticeConverter.toPagedNoticeInfo(notices);
    }
}
