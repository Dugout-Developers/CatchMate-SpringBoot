package com.back.catchmate.domain.chat.dto;

import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

public abstract class ChatResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageInfo {
        ObjectId id;
        private Long roomId;
        private String content;
        private Long senderId;
        private MessageType messageType;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedChatMessageInfo {
        private List<ChatMessageInfo> chatMessageInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatRoomInfo {
        private Long chatRoomId;
        private BoardInfo boardInfo;
        private Integer participantCount;
        private LocalDateTime lastMessageAt;
        private String lastMessageContent;
        private String chatRoomImage;
        private Integer unreadMessageCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedChatRoomInfo {
        private List<ChatRoomInfo> chatRoomInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }
}
