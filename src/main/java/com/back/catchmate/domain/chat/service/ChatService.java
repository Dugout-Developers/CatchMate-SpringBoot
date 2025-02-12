package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatMessageInfo;
import org.springframework.data.domain.Pageable;

public interface ChatService {
    void sendChatMessage(Long chatRoomId, ChatMessageRequest request);

    void sendEnterLeaveMessage(Long chatRoomId, String content, Long senderId, ChatMessageRequest.MessageType messageType);

    PagedChatMessageInfo getChatMessageList(Long userId, Long roomId, Pageable pageable);
}
