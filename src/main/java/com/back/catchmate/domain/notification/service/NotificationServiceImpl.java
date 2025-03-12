package com.back.catchmate.domain.notification.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.inquiry.entity.Inquiry;
import com.back.catchmate.domain.inquiry.repository.InquiryRepository;
import com.back.catchmate.domain.notification.converter.NotificationConverter;
import com.back.catchmate.domain.notification.dto.NotificationResponse.NotificationInfo;
import com.back.catchmate.domain.notification.dto.NotificationResponse.PagedNotificationInfo;
import com.back.catchmate.domain.notification.entity.Notification;
import com.back.catchmate.domain.notification.repository.NotificationRepository;
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

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final NotificationRepository notificationRepository;
    private final InquiryRepository inquiryRepository;
    private final NotificationConverter notificationConverter;

    @Override
    @Transactional
    public void createNotification(String title, String body, String senderProfileImageUrl, Long boardId, Long userId, AcceptStatus acceptStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        Notification notification = notificationConverter.toEntity(user, board, senderProfileImageUrl, title, body, acceptStatus);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createNotification(String title, String body, String senderProfileImageUrl, Long inquiryId, Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findByIdAndDeletedAtIsNull(inquiryId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        Notification notification = notificationConverter.toEntity(user, inquiry, senderProfileImageUrl, title, body);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedNotificationInfo getNotificationList(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Notification> notificationList = notificationRepository.findByUserIdAndDeletedAtIsNull(user.getId(), pageable);
        return notificationConverter.toPagedNotificationInfo(notificationList);
    }

    @Override
    @Transactional
    public NotificationInfo getNotification(Long userId, Long notificationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationRepository.findByIdAndUserIdAndDeletedAtIsNull(notificationId, user.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 읽지 않은 알림일 경우, 읽음으로 표시
        if (notification.isNotRead()) {
            notification.markAsRead();
        }

        return notificationConverter.toNotificationInfo(notification, notification.getBoard());
    }

    @Override
    @Transactional
    public StateResponse deleteNotification(Long userId, Long notificationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationRepository.findByIdAndUserIdAndDeletedAtIsNull(notificationId, user.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.delete();
        return new StateResponse(true);
    }

    @Override
    public Boolean hasUnreadNotification(Long userId) {
        return notificationRepository.existsByUserIdAndIsReadFalseAndDeletedAtIsNull(userId);
    }
}
