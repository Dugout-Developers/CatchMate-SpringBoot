package com.back.catchmate.domain.chat.controller;

import com.back.catchmate.domain.chat.dto.ChatRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.MessageInfo;
import com.back.catchmate.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatRequest.ChatMessageRequest request) {
        chatService.sendMessage(request);
    }

    @GetMapping(value = "/chat/chatRoom/{roomId}")
    public Mono<List<MessageInfo>> find(@PathVariable("roomId") Long roomId) {
        return chatService.findChatMessageList(roomId).collectList();
    }
}
