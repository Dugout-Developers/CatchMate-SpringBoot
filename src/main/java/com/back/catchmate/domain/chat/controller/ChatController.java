package com.back.catchmate.domain.chat.controller;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.MessageInfo;
import com.back.catchmate.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "채팅 관련 API")
@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat.{chatRoomId}")
    @SendTo("/topic/chat.{chatRoomId}")
    public void sendMessage(@DestinationVariable Long chatRoomId, ChatMessageRequest request) {
        chatService.sendMessage(chatRoomId, request);
    }

    @Deprecated
    @GetMapping(value = "/chat/chatRoom/{roomId}")
    public Mono<List<MessageInfo>> findChatMessageList(@PathVariable("roomId") Long roomId) {
        return chatService.findChatMessageList(roomId).collectList();
    }
}
