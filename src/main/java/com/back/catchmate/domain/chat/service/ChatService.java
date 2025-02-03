package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface ChatService {
    void sendMessage(Long chatRoomId, ChatMessageRequest request);

    Mono<ChatResponse.PagedChatMessageInfo> getChatMessageList(Long userId, Long roomId, Pageable pageable);
}
