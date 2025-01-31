package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.MessageInfo;
import reactor.core.publisher.Flux;

public interface ChatService {
    void sendMessage(Long chatRoomId, ChatMessageRequest request);

    Flux<MessageInfo> findChatMessageList(Long roomId);
}
