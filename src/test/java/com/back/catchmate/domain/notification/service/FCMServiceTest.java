package com.back.catchmate.domain.notification.service;

import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.notification.dto.FCMMessageRequest;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FCMServiceTest {

    @InjectMocks
    private FCMService fcmService;

    @Mock
    private UserChatRoomRepository userChatRoomRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OkHttpClient okHttpClient;

    @Mock private FcmTokenProvider fcmTokenProvider;

    @Mock
    private Call call;

    @BeforeEach
    void setUp() {
        // @Value 필드는 Mockito가 주입해주지 않으므로 ReflectionTestUtils로 직접 주입
        ReflectionTestUtils.setField(fcmService, "FIREBASE_ALARM_SEND_API_URI", "http://test-firebase-uri");
        ReflectionTestUtils.setField(fcmService, "FIREBASE_CONFIG_PATH", "firebase/test-key.json"); // 테스트용 가짜 경로
    }

    // 1. makeEnrollMessage 테스트 (JSON 생성 로직 검증)
    @Test
    @DisplayName("makeEnrollMessage - 입력된 정보가 올바른 JSON 포맷으로 변환되어야 한다")
    void makeEnrollMessage_Success() throws Exception {
        // given
        String token = "test_fcm_token";
        String title = "직관 신청";
        String body = "신청이 도착했습니다.";
        Long boardId = 1L;
        Long chatRoomId = 100L;
        AcceptStatus status = AcceptStatus.PENDING;

        // when
        String jsonResult = fcmService.makeEnrollMessage(token, title, body, boardId, status, chatRoomId);

        // then
        // 생성된 JSON 문자열을 다시 객체로 변환하여 값 검증
        FCMMessageRequest request = objectMapper.readValue(jsonResult, FCMMessageRequest.class);

        assertThat(request.getMessage().getToken()).isEqualTo(token);
        assertThat(request.getMessage().getNotification().getTitle()).isEqualTo(title);
        assertThat(request.getMessage().getNotification().getBody()).isEqualTo(body);

        // Data 필드는 모두 String으로 변환되어야 함
        assertThat(request.getMessage().getData().getBoardId()).isEqualTo("1");
        assertThat(request.getMessage().getData().getChatRoomId()).isEqualTo("100");
        assertThat(request.getMessage().getData().getAcceptStatus()).isEqualTo(AcceptStatus.PENDING);
    }

    // 2. makeInquiryMessage 테스트 (JSON 생성 로직 검증)
    @Test
    @DisplayName("makeInquiryMessage - 입력된 정보가 올바른 JSON 포맷으로 변환되어야 한다")
    void makeInquiryMessage_Success() throws Exception {
        // given
        String token = "test_fcm_token";
        String title = "문의 답변";
        String body = "답변이 등록되었습니다.";
        Long inquiryId = 50L;

        // when
        String jsonResult = fcmService.makeInquiryMessage(token, title, body, inquiryId);

        // then
        FCMMessageRequest request = objectMapper.readValue(jsonResult, FCMMessageRequest.class);

        assertThat(request.getMessage().getToken()).isEqualTo(token);
        assertThat(request.getMessage().getNotification().getTitle()).isEqualTo(title);
        assertThat(request.getMessage().getData().getBoardId()).isEqualTo("50"); // inquiryId가 boardId 필드에 매핑됨
    }

    // 3. sendMessagesByTokens 테스트 (토큰 필터링 로직 검증)
    @Test
    @DisplayName("sendMessagesByTokens - 보낸 사람과 토큰이 없는 유저는 발송 대상에서 제외되어야 한다")
    void sendMessagesByTokens_FilterLogic_Success() throws FirebaseMessagingException {
        // given
        Long chatRoomId = 1L;
        String senderToken = "sender_token";

        // 상황: 채팅방에 3명이 존재
        // 1. Sender (제외되어야 함)
        User sender = mock(User.class);
        given(sender.getFcmToken()).willReturn(senderToken);
        UserChatRoom ucrSender = mock(UserChatRoom.class);
        given(ucrSender.getUser()).willReturn(sender);

        // 2. Token 없는 유저 (제외되어야 함)
        User noTokenUser = mock(User.class);
        given(noTokenUser.getFcmToken()).willReturn(null);
        UserChatRoom ucrNoToken = mock(UserChatRoom.class);
        given(ucrNoToken.getUser()).willReturn(noTokenUser);

        // 3. Valid Receiver (포함되어야 함)
        User receiver = mock(User.class);
        given(receiver.getFcmToken()).willReturn("receiver_token");
        UserChatRoom ucrReceiver = mock(UserChatRoom.class);
        given(ucrReceiver.getUser()).willReturn(receiver);

        // Repository Stubbing
        given(userChatRoomRepository.findByChatRoomIdAndDeletedAtIsNull(chatRoomId))
                .willReturn(List.of(ucrSender, ucrNoToken, ucrReceiver));

        // FirebaseMessaging Static Method Mocking (실제 전송 방지)
        try (MockedStatic<FirebaseMessaging> firebaseMessagingMock = mockStatic(FirebaseMessaging.class)) {
            FirebaseMessaging messagingInstance = mock(FirebaseMessaging.class);
            firebaseMessagingMock.when(FirebaseMessaging::getInstance).thenReturn(messagingInstance);

            // BatchResponse Mocking
            BatchResponse batchResponse = mock(BatchResponse.class);
            given(batchResponse.getSuccessCount()).willReturn(1);
            given(messagingInstance.sendEachForMulticast(any(MulticastMessage.class))).willReturn(batchResponse);

            // when
            fcmService.sendMessagesByTokens(chatRoomId, "title", "body", senderToken);

            // then
            // sendEachForMulticast가 호출되었을 때 넘어간 인자(MulticastMessage)를 캡처하거나 검증하지는 못하지만(private field),
            // 로직상 필터링 후 list가 비어있지 않아야 메서드가 호출됨을 간접 확인
            verify(messagingInstance, times(1)).sendEachForMulticast(any(MulticastMessage.class));
        }
    }

    @Test
    @DisplayName("sendMessageByToken - 외부 API 호출 및 토큰 주입 로직 검증")
    void sendMessageByToken_Success() throws IOException {
        // given
        // @Value 필드 주입 (ReflectionTestUtils 사용)
        ReflectionTestUtils.setField(fcmService, "FIREBASE_ALARM_SEND_API_URI", "https://fcm.googleapis.com/v1/projects/test/messages:send");

        String targetToken = "target_token";
        String title = "제목";
        String body = "내용";
        Long inquiryId = 1L;

        // 1. 토큰 제공자 Mocking (파일 읽기 없이 토큰 반환)
        given(fcmTokenProvider.getAccessToken()).willReturn("mock-access-token");

        // 2. OkHttpClient Mocking (실제 전송 없이 성공 응답 반환)
        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("https://fcm.googleapis.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{\"name\":\"projects/test/messages/123\"}", MediaType.get("application/json")))
                .build();

        given(okHttpClient.newCall(any(Request.class))).willReturn(mockCall);
        given(mockCall.execute()).willReturn(mockResponse);

        // when
        fcmService.sendMessageByToken(targetToken, title, body, inquiryId);

        // then
        // OkHttpClient가 호출되었는지 검증
        verify(okHttpClient).newCall(any(Request.class));
        // 토큰을 가져오기 위해 Provider가 호출되었는지 검증
        verify(fcmTokenProvider).getAccessToken();
    }

    @Test
    @DisplayName("sendMessageByToken (Enroll) - 직관 신청 알림 전송 요청이 올바르게 수행되어야 한다")
    void sendMessageByToken_Enroll_Success() throws IOException {
        // given
        // @Value 필드 주입
        ReflectionTestUtils.setField(fcmService, "FIREBASE_ALARM_SEND_API_URI", "https://fcm.googleapis.com/v1/projects/test/messages:send");

        String targetToken = "target_token";
        String title = "직관 신청";
        String body = "신청이 왔습니다.";
        Long boardId = 1L;
        AcceptStatus status = AcceptStatus.PENDING;
        Long chatRoomId = 100L;

        // 1. 토큰 제공자 Mocking
        given(fcmTokenProvider.getAccessToken()).willReturn("mock-access-token");

        // 2. OkHttpClient Mocking (성공 응답 설정)
        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("https://fcm.googleapis.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{\"name\":\"projects/test/messages/123\"}", MediaType.get("application/json")))
                .build();

        given(okHttpClient.newCall(any(Request.class))).willReturn(mockCall);
        given(mockCall.execute()).willReturn(mockResponse);

        // when
        fcmService.sendMessageByToken(targetToken, title, body, boardId, status, chatRoomId);

        // then
        // 1. OkHttpClient가 호출되었는지 검증
        verify(okHttpClient).newCall(any(Request.class));

        // 2. 토큰 Provider가 호출되었는지 검증
        verify(fcmTokenProvider).getAccessToken();
    }

    @Test
    @DisplayName("FCM 응답 바디가 null일 경우 BaseException(EMPTY_FCM_RESPONSE)이 발생해야 한다")
    void sendMessageByToken_Fail_EmptyResponse() throws IOException {
        // given
        // 1. 필요한 설정 주입 (API URL 등)
        ReflectionTestUtils.setField(fcmService, "FIREBASE_ALARM_SEND_API_URI", "https://fcm.googleapis.com/v1/projects/test/messages:send");

        String targetToken = "target_token";
        String title = "제목";
        String body = "내용";
        Long boardId = 1L;
        AcceptStatus status = AcceptStatus.PENDING;
        Long chatRoomId = 100L;

        // 2. 토큰 Provider Mocking (정상 토큰 반환)
        given(fcmTokenProvider.getAccessToken()).willReturn("mock-access-token");

        // 3. OkHttp Response Mocking (Body가 없는 응답 생성)
        // Response.Builder()로 body를 설정하지 않으면 기본적으로 null입니다.
        Call mockCall = mock(Call.class);
        Response nullBodyResponse = new Response.Builder()
                .request(new Request.Builder().url("https://fcm.googleapis.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                // .body(...) // body를 설정하지 않음 -> null
                .build();

        given(okHttpClient.newCall(any(Request.class))).willReturn(mockCall);
        given(mockCall.execute()).willReturn(nullBodyResponse);

        // when & then
        assertThatThrownBy(() -> fcmService.sendMessageByToken(targetToken, title, body, boardId, status, chatRoomId))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.EMPTY_FCM_RESPONSE.getMessage());
    }
}
