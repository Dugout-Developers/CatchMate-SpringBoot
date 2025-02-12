package com.back.catchmate.domain.notification.service;

import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.notification.dto.FCMMessageRequest;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
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
import java.util.Collections;
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

    // Firebase로 부터 Access Token을 가져오는 메서드
    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(FIREBASE_CONFIG_PATH).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();

        return googleCredentials.getAccessToken().getTokenValue();
    }

    // 알림 파라미터들을 요구하는 body 형태로 가공
    public String makeMessage(String targetToken, String title, String body, Long boardId, AcceptStatus acceptStatus) throws JsonProcessingException {
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
        String message = makeMessage(targetToken, title, body, boardId, acceptStatus);

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

    public void sendMessageToTopic(String topic, String title, String body) {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setTopic(topic)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message: {}", response);
        } catch (FirebaseMessagingException e) {
            throw new BaseException(ErrorCode.FCM_SUBSCRIBE_BAD_REQUEST);
        }
    }

    public void subscribeToTopic(String fcmToken, Long chatRoomId) {
        try {
            String topic = "chat_room_" + chatRoomId;
            FirebaseMessaging.getInstance().subscribeToTopic(Collections.singletonList(fcmToken), topic);
        } catch (FirebaseMessagingException e) {
            throw new BaseException(ErrorCode.FCM_SUBSCRIBE_BAD_REQUEST);
        }
    }

    public void unsubscribeFromTopic(String fcmToken, Long chatRoomId) {
        try {
            String topic = "chat_room_" + chatRoomId;
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Collections.singletonList(fcmToken), topic);
        } catch (FirebaseMessagingException e) {
            throw new BaseException(ErrorCode.FCM_UNSUBSCRIBE_BAD_REQUEST);
        }
    }
}
