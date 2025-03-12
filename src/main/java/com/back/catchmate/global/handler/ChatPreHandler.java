package com.back.catchmate.global.handler;

import com.back.catchmate.domain.chat.service.ChatSessionService;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatPreHandler implements ChannelInterceptor {
    private final JwtService jwtService;
    private final ChatSessionService chatSessionService; // 사용자 접속 관리

    private static final String ACCESS_TOKEN_HEADER = "AccessToken";

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> headers = accessor.getNativeHeader(ACCESS_TOKEN_HEADER);
            try {
                if (!CollectionUtils.isEmpty(headers)) {
                    Long userId = jwtService.parseJwtToken(headers.get(0));
                    Long chatRoomId = getChatRoomIdFromHeaders(accessor); // 채팅방 ID 추출
                    chatSessionService.userJoined(chatRoomId, userId); // 접속 정보 저장

                    log.info("User connected: userId={}, chatRoomId={}", userId, chatRoomId);
                }
            } catch (MessageDeliveryException e) {
                log.error("WebSocket connection failed: {}", e.getMessage());
                throw new BaseException(ErrorCode.SOCKET_CONNECT_FAILED);
            }
        }

        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            Long userId = getUserIdFromSession(accessor);
            Long chatRoomId = getChatRoomIdFromSession(accessor);
            if (userId != null && chatRoomId != null) {
                chatSessionService.userLeft(chatRoomId, userId); // 접속 정보 삭제
                log.info("User disconnected: userId={}, chatRoomId={}", userId, chatRoomId);
            } else {
                log.warn("User disconnected but session info is missing.");
            }
        }

        return message;
    }

    private Long getChatRoomIdFromHeaders(StompHeaderAccessor accessor) {
        List<String> chatRoomHeaders = accessor.getNativeHeader("ChatRoomId");
        return (chatRoomHeaders != null && !chatRoomHeaders.isEmpty()) ? Long.valueOf(chatRoomHeaders.get(0)) : null;
    }

    private Long getUserIdFromSession(StompHeaderAccessor accessor) {
        return (Long) accessor.getSessionAttributes().get("userId");
    }

    private Long getChatRoomIdFromSession(StompHeaderAccessor accessor) {
        return (Long) accessor.getSessionAttributes().get("chatRoomId");
    }
}
