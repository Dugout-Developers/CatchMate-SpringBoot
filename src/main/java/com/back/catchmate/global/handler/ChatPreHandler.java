package com.back.catchmate.global.handler;

import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Component
public class ChatPreHandler implements ChannelInterceptor {
    private final JwtService jwtService;

    private static final String ACCESS_TOKEN_HEADER = "AccessToken";

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(message);

            List<String> headers = stompHeaderAccessor.getNativeHeader(ACCESS_TOKEN_HEADER);
            // 토큰 인증
            try {
                if (!CollectionUtils.isEmpty(headers)) {
                    Long token = jwtService.parseJwtToken(headers.get(0));
                }
            } catch (MessageDeliveryException e) {
                throw new BaseException(ErrorCode.SOCKET_CONNECT_FAILED);
            }
        }

        return message;
    }
}
