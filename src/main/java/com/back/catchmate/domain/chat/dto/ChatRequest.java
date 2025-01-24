package com.back.catchmate.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public abstract class ChatRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRequest {
        public enum MessageType {
            ENTER, TALK, LEAVE;
        }

        private MessageType messageType;
        private Long chatRoomId;
        private String sender;   // 보낸 사람
        private String content;  // 메시지 내용
        private LocalDateTime sendTime;
    }
}
