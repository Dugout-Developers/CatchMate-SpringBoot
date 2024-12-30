package com.back.catchmate.domain.notification.converter;

import com.back.catchmate.domain.notification.dto.NotificationResponse.CreateNotificationInfo;
import com.back.catchmate.domain.notification.entity.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationConverter {
    public CreateNotificationInfo toCreateNotificationInfo(Notification notification) {
        return CreateNotificationInfo.builder()
                .notificationId(notification.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
