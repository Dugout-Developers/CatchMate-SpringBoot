package com.back.catchmate.domain.notification.service;

import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.notification.dto.FCMMessageRequest;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {
    @Value("${fcm.firebase_config_path}")
    private String FIREBASE_CONFIG_PATH;
    @Value("${fcm.firebase_api_uri}")
    private String FIREBASE_ALARM_SEND_API_URI;

    private final ObjectMapper objectMapper;
    private final UserChatRoomRepository userChatRoomRepository;

    // Firebase로 부터 Access Token을 가져오는 메서드
    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(FIREBASE_CONFIG_PATH).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();

        return googleCredentials.getAccessToken().getTokenValue();
    }

    // 신청 알림 파라미터들을 요구하는 body 형태로 가공
    public String makeEnrollMessage(String targetToken, String title, String body, Long boardId, AcceptStatus acceptStatus) throws JsonProcessingException {
        FCMMessageRequest fcmMessage = FCMMessageRequest.builder()
                .message(
                        FCMMessageRequest.Message.builder()
                                .token(targetToken)
                                .notification(
                                        FCMMessageRequest.Notification.builder()
                                                .title(title)
                                                .body(body)
                                                .build()
                                )
                                .data(
                                        FCMMessageRequest.Data.builder()
                                                .boardId(String.valueOf(boardId))
                                                .acceptStatus(acceptStatus)
                                                .build()
                                )
                                .build()
                )
                .validateOnly(false)
                .build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    // 사용자의 FCM 토큰을 사용하여 푸쉬 알림을 보내는 역할을 하는 메서드
    @Async("asyncTask")
    public void sendMessageByToken(String targetToken, String title, String body, Long boardId, AcceptStatus acceptStatus) throws IOException {
        String message = makeEnrollMessage(targetToken, title, body, boardId, acceptStatus);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(FIREBASE_ALARM_SEND_API_URI)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        if (response.body() == null) {
            throw new BaseException(ErrorCode.EMPTY_FCM_RESPONSE);
        }

        log.info(response.body().string());
    }

    // 특정 채팅방의 모든 사용자에게 FCM 메시지 전송
    @Async("asyncTask")
    public void sendMessagesByTokens(Long chatRoomId, String title, String body, String senderToken) throws IOException, FirebaseMessagingException {
        List<String> targetTokenList = userChatRoomRepository.findByChatRoomIdAndDeletedAtIsNull(chatRoomId)
                .stream()
                .map(userChatRoom -> userChatRoom.getUser().getFcmToken()) // User 엔티티에서 FCM 토큰 가져오기
                .filter(token -> token != null && !token.isEmpty())
                .filter(token -> !token.equals(senderToken))
                .toList();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("chatRoomId", String.valueOf(chatRoomId))
                .addAllTokens(targetTokenList)
                .build();

        // FCM에 메시지 전송
        BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

        // 전송 결과 확인
        if (response.getFailureCount() > 0) {
            log.error("일부 메시지 전송에 실패했습니다. 성공한 메시지 수: {}, 실패한 메시지 수: {}",
                    response.getSuccessCount(), response.getFailureCount());
            throw new BaseException(ErrorCode.FCM_TOKEN_SEND_BAD_REQUEST);
        }

        log.info("FCM 응답: {}개의 메시지가 성공적으로 전송되었습니다.", response.getSuccessCount());
    }
}
