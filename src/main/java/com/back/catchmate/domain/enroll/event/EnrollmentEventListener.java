package com.back.catchmate.domain.enroll.event;

import com.back.catchmate.domain.notification.service.FCMService;
import com.back.catchmate.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final FCMService fcmService;
    private final NotificationService notificationService;

    @TransactionalEventListener
    public void handleEnrollmentAcceptedEvent(EnrollmentAcceptedEvent event) {
        try {
            // 1. FCM 푸시 알림 전송
            fcmService.sendMessageByToken(
                    event.getFcmToken(),
                    event.getTitle(),
                    event.getBody(),
                    event.getBoardId(),
                    event.getAcceptStatus(),
                    event.getChatRoomId()
            );

            // 2. DB에 알림 내역 저장
            notificationService.createNotification(
                    event.getTitle(),
                    event.getBody(),
                    event.getSenderId(),
                    event.getBoardId(),
                    event.getReceiverId(),
                    event.getAcceptStatus()
            );
        } catch (IOException e) {
            // FCM 전송 실패 시 로그를 남기고, DB 작업은 이미 커밋되었으므로 롤백되지 않음.
            log.error("Failed to send FCM message for accepted enrollment", e);
            // 필요하다면 여기에 재시도 로직이나 실패 알림 로직을 추가할 수 있습니다.
        }
    }

    @TransactionalEventListener
    public void handleEnrollmentRejectedEvent(EnrollmentRejectedEvent event) {
        try {
            // 1. FCM 푸시 알림 전송
            fcmService.sendMessageByToken(
                    event.getFcmToken(),
                    event.getTitle(),
                    event.getBody(),
                    event.getBoardId(),
                    event.getAcceptStatus(),
                    null // 거절 시에는 채팅방 ID가 없음
            );

            // 2. DB에 알림 내역 저장
            notificationService.createNotification(
                    event.getTitle(),
                    event.getBody(),
                    event.getSenderId(),
                    event.getBoardId(),
                    event.getReceiverId(),
                    event.getAcceptStatus()
            );
        } catch (IOException e) {
            log.error("Failed to send FCM message for rejected enrollment", e);
        }
    }
}
