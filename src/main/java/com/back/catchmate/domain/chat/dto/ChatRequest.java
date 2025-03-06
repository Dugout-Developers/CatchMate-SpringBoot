package com.back.catchmate.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class ChatRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRequest {
        public enum MessageType {
            ENTER, TALK, LEAVE, DATE;
        }

        private MessageType messageType;
        private Long senderId;   // 보낸 사람
        private String content;  // 메시지 내용
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadChatMessageRequest {
        private Long chatRoomId;   // 보낸 사람
        private Long userId;  // 메시지 내용
    }
}
