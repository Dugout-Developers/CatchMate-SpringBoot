package com.back.catchmate.domain.admin.notice.service;

import com.back.catchmate.domain.admin.notice.converter.AdminNoticeConverter;
import com.back.catchmate.domain.admin.notice.dto.NoticeRequest;
import com.back.catchmate.domain.admin.notice.dto.NoticeResponse;
import com.back.catchmate.domain.notice.entity.Notice;
import com.back.catchmate.domain.notice.repository.NoticeRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
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
public class AdminNoticeServiceImpl implements AdminNoticeService {
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;
    private final AdminNoticeConverter adminNoticeConverter;

    @Override
    @Transactional
    public NoticeResponse.NoticeInfo createNotice(Long userId, NoticeRequest.CreateNoticeRequest noticeRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Notice notice = adminNoticeConverter.toEntity(user, noticeRequest);
        notice = noticeRepository.save(notice);

        return adminNoticeConverter.toNoticeInfo(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponse.NoticeInfo getNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdAndDeletedAtIsNull(noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

        return adminNoticeConverter.toNoticeInfo(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponse.PagedNoticeInfo getNoticeList(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        Page<Notice> notices = noticeRepository.findNoticesWithinDateRange(startDateTime, endDateTime, pageable);
        return adminNoticeConverter.toPagedNoticeInfo(notices);
    }

    @Override
    @Transactional
    public NoticeResponse.NoticeInfo updateNotice(Long userId, Long noticeId, NoticeRequest.UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

//        if (!notice.getUser().getId().equals(userId)) {
//            throw new BaseException(ErrorCode.FORBIDDEN);
//        }

        notice.updateNotice(request.getTitle(), request.getContent());
        return adminNoticeConverter.toNoticeInfo(notice);
    }

    @Override
    @Transactional
    public StateResponse deleteNotice(Long userId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

//        if (!notice.getUser().getId().equals(userId)) {
//            throw new BaseException(ErrorCode.FORBIDDEN);
//        }

        notice.delete();
        return new StateResponse(true);
    }
}
