package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.dto.ChatRequest;
import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatMessageInfo;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface ChatService {
    void sendChatMessage(Long chatRoomId, ChatMessageRequest request) throws IOException, FirebaseMessagingException;

    void updateLastReadTime(ChatRequest.ReadChatMessageRequest request);

    void sendEnterLeaveMessage(Long chatRoomId, String content, Long senderId, ChatMessageRequest.MessageType messageType);

    PagedChatMessageInfo getChatMessageList(Long userId, Long roomId, Pageable pageable);
}
