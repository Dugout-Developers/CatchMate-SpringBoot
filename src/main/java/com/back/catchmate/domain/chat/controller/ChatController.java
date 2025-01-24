package com.back.catchmate.domain.chat.controller;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.MessageInfo;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatRoomInfo;
import com.back.catchmate.domain.chat.service.ChatService;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "채팅 관련 API")
@Slf4j
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

    @GetMapping("/list")
    @Operation(summary = "내가 속한 채팅방 조회 API", description = "내가 속해있는 채팅방을 조회하는 API 입니다.")
    public PagedChatRoomInfo getChatRoomList(@JwtValidation Long userId,
                                             @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                             @Parameter(hidden = true) Pageable pageable) {
        return chatService.getChatRoomList(userId, pageable);
    }

    @Deprecated
    @GetMapping(value = "/chat/chatRoom/{roomId}")
    public Mono<List<MessageInfo>> findChatMessageList(@PathVariable("roomId") Long roomId) {
        return chatService.findChatMessageList(roomId).collectList();
    }
}
