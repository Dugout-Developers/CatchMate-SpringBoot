package com.back.catchmate.domain.notification.dto;

import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FCMMessageRequest {
    private boolean validateOnly;
    private Message message;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Message {
        private Notification notification;
        private String token;
        private Data data;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Notification {
        private String title;
        private String body;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Data {
        private String boardId;
        private String chatRoomId;
        private AcceptStatus acceptStatus;
    }
}
