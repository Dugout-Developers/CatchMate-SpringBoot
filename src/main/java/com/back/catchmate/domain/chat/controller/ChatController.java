package com.back.catchmate.domain.chat.controller;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatRequest.ReadChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatMessageInfo;
import com.back.catchmate.domain.chat.service.ChatService;
import com.back.catchmate.global.jwt.JwtValidation;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@Tag(name = "[사용자] 채팅 관련 API")
@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @SendTo("/topic/chat.{chatRoomId}")
    @MessageMapping("/chat.{chatRoomId}")
    public void sendMessage(@DestinationVariable Long chatRoomId,
                            @Payload ChatMessageRequest request) throws IOException, FirebaseMessagingException {
        chatService.sendChatMessage(chatRoomId, request);
    }

    @MessageMapping("/chat/read")
    public void updateLastReadTime(@Payload ReadChatMessageRequest request) {
        log.info("Received chat read request: {}", request);
        chatService.updateLastReadTime(request);
    }

    @GetMapping("/{chatRoomId}")
    @Operation(summary = "특정 채팅방의 채팅 내역 조회 API", description = "특정 채팅방의 채팅 내역 조회 API 입니다.")
    public PagedChatMessageInfo findChatMessageList(@JwtValidation Long userId,
                                                    @PathVariable Long chatRoomId,
                                                    @RequestParam(required = false) String lastMessageId, // 마지막 메시지 ID
                                                    @RequestParam(defaultValue = "20") int size) {
        return chatService.getChatMessageList(userId, chatRoomId, lastMessageId, size);
    }

}
