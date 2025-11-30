package com.back.catchmate.domain.enroll.event;

import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.notification.service.FCMService;
import com.back.catchmate.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentEventListenerTest {

    @InjectMocks
    private EnrollmentEventListener enrollmentEventListener;

    @Mock
    private FCMService fcmService;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("수락 이벤트 수신 시 FCM 전송과 알림 저장이 순차적으로 호출되어야 한다")
    void handleEnrollmentAcceptedEvent_Success() throws IOException {
        // given
        EnrollmentAcceptedEvent event = mock(EnrollmentAcceptedEvent.class);
        given(event.getFcmToken()).willReturn("token");
        given(event.getTitle()).willReturn("수락 알림");
        given(event.getBody()).willReturn("수락되었습니다.");
        given(event.getBoardId()).willReturn(1L);
        given(event.getAcceptStatus()).willReturn(AcceptStatus.ACCEPTED);
        given(event.getChatRoomId()).willReturn(100L);
        given(event.getSenderId()).willReturn(2L);
        given(event.getReceiverId()).willReturn(3L);

        // when
        enrollmentEventListener.handleEnrollmentAcceptedEvent(event);

        // then
        // 1. FCM 전송 호출 검증 (chatRoomId 포함)
        verify(fcmService).sendMessageByToken(
                eq("token"), eq("수락 알림"), eq("수락되었습니다."),
                eq(1L), eq(AcceptStatus.ACCEPTED), eq(100L)
        );

        // 2. DB 저장 호출 검증
        verify(notificationService).createNotification(
                eq("수락 알림"), eq("수락되었습니다."),
                eq(2L), eq(1L), eq(3L), eq(AcceptStatus.ACCEPTED)
        );
    }

    @Test
    @DisplayName("거절 이벤트 수신 시 FCM 전송(chatRoomId=null)과 알림 저장이 호출되어야 한다")
    void handleEnrollmentRejectedEvent_Success() throws IOException {
        // given
        EnrollmentRejectedEvent event = mock(EnrollmentRejectedEvent.class);
        given(event.getFcmToken()).willReturn("token");
        given(event.getTitle()).willReturn("거절 알림");
        given(event.getBody()).willReturn("거절되었습니다.");
        given(event.getBoardId()).willReturn(1L);
        given(event.getAcceptStatus()).willReturn(AcceptStatus.REJECTED);
        given(event.getSenderId()).willReturn(2L);
        given(event.getReceiverId()).willReturn(3L);

        // when
        enrollmentEventListener.handleEnrollmentRejectedEvent(event);

        // then
        // 1. FCM 전송 호출 검증 (거절 시 chatRoomId는 null이어야 함)
        verify(fcmService).sendMessageByToken(
                eq("token"), eq("거절 알림"), eq("거절되었습니다."),
                eq(1L), eq(AcceptStatus.REJECTED), eq(null)
        );

        // 2. DB 저장 호출 검증
        verify(notificationService).createNotification(
                eq("거절 알림"), eq("거절되었습니다."),
                eq(2L), eq(1L), eq(3L), eq(AcceptStatus.REJECTED)
        );
    }

    @Test
    @DisplayName("FCM 전송 중 예외가 발생해도 로그만 남기고 종료되어야 한다 (예외 전파 X)")
    void handleEnrollmentAcceptedEvent_Exception() throws IOException {
        // given
        EnrollmentAcceptedEvent event = mock(EnrollmentAcceptedEvent.class);
        given(event.getFcmToken()).willReturn("token");

        // FCM 서비스에서 IOException 발생 설정
        doThrow(new IOException("FCM Error")).when(fcmService).sendMessageByToken(any(), any(), any(), any(), any(), any());

        // when
        // 예외가 발생하지 않아야 함 (try-catch로 잡음)
        enrollmentEventListener.handleEnrollmentAcceptedEvent(event);

        // then
        // 1. FCM 시도는 했는지 확인
        verify(fcmService).sendMessageByToken(any(), any(), any(), any(), any(), any());

        // 2. 예외 발생 시 이후 로직(Notification 저장)은 실행되지 않음 (try 블록 내부 로직에 따라 다름)
        // 코드 구조상: fcmService 호출 -> notificationService 호출 순서이고,
        // 전체가 하나의 try 블록에 있으므로 fcm에서 터지면 notification은 호출 안 됨.
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any(), any());
    }
}
