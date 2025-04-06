package com.back.catchmate.domain.notification.service;

import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.notification.dto.NotificationResponse.NotificationInfo;
import com.back.catchmate.domain.notification.dto.NotificationResponse.PagedNotificationInfo;
import com.back.catchmate.global.dto.StateResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void createNotification(String title, String body, Long senderId, Long boardId, Long receiverId, AcceptStatus acceptStatus);

    void createNotification(String title, String body, Long senderId, Long inquiryId, Long receiverId);

    PagedNotificationInfo getNotificationList(Long userId, Pageable pageable);

    NotificationInfo getNotification(Long userId, Long notificationId);

    StateResponse deleteNotification(Long userId, Long notificationId);

    Boolean hasUnreadNotification(Long userId);
}
