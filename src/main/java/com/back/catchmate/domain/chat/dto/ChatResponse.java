package com.back.catchmate.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

public abstract class ChatResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageInfo {
        ObjectId id;
        private Long roomId;
        private String content;
        private String sender;
    }
}
