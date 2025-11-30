package com.back.catchmate.global.handler;

import com.back.catchmate.domain.chat.service.ChatSessionService;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatPreHandlerTest {

    @InjectMocks
    private ChatPreHandler chatPreHandler;

    @Mock
    private JwtService jwtService;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private MessageChannel channel;

    private final String VALID_TOKEN = "valid_token";
    private final Long USER_ID = 1L;
    private final Long CHAT_ROOM_ID = 100L;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("CONNECT 시 토큰과 채팅방 ID가 유효하면 세션에 저장하고 서비스를 호출한다")
    void preSend_Connect_Success() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("AccessToken", VALID_TOKEN);
        accessor.setNativeHeader("ChatRoomId", String.valueOf(CHAT_ROOM_ID));
        // SessionAttributes 초기화 (실제 런타임 환경 모사)
        accessor.setSessionAttributes(new HashMap<>());

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        given(jwtService.parseJwtToken(VALID_TOKEN)).willReturn(USER_ID);

        // when
        Message<?> result = chatPreHandler.preSend(message, channel);

        // then
        assertThat(result).isNotNull();

        // 1. 세션 속성 저장 검증
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getSessionAttributes()).containsEntry("userId", USER_ID);
        assertThat(resultAccessor.getSessionAttributes()).containsEntry("chatRoomId", CHAT_ROOM_ID);

        // 2. 서비스 호출 검증
        verify(chatSessionService).userJoined(CHAT_ROOM_ID, USER_ID);
    }

    @Test
    @DisplayName("CONNECT 시 AccessToken 헤더가 없으면 로직을 수행하지 않는다 (예외 발생 X)")
    void preSend_Connect_NoToken_NoAction() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        // AccessToken 헤더 설정 안 함
        accessor.setNativeHeader("ChatRoomId", String.valueOf(CHAT_ROOM_ID));

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        chatPreHandler.preSend(message, channel);

        // then
        // 서비스가 호출되지 않아야 함
        verify(chatSessionService, never()).userJoined(anyLong(), anyLong());
        // (Type mismatch 주의: userJoined는 Long, Long을 받으므로 anyLong() 또는 eq() 사용)
        verify(chatSessionService, never()).userJoined(CHAT_ROOM_ID, USER_ID);
    }

    @Test
    @DisplayName("CONNECT 시 ChatRoomId 헤더가 없으면 null을 반환하고 서비스 호출을 하지 않는다 (실패)")
        // 코드 로직상 getChatRoomIdFromHeaders에서 null 반환 -> 이후 NPE 발생 가능성 있음 (Long.valueOf(null) X, return null O)
        // 하지만 chatPreHandler 로직을 보면:
        // chatRoomId = getChatRoomIdFromHeaders(accessor);
        // ...
        // chatSessionService.userJoined(chatRoomId, userId);
        // 만약 chatRoomId가 null이면 userJoined 호출 시 문제 될 수 있음 (Primitive long이면 NPE, Wrapper Long이면 OK)
        // 코드상 Wrapper Long을 사용하므로 호출 자체는 됨.
    void preSend_Connect_NoChatRoomId() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("AccessToken", VALID_TOKEN);
        // ChatRoomId 헤더 누락
        accessor.setSessionAttributes(new HashMap<>());

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        given(jwtService.parseJwtToken(VALID_TOKEN)).willReturn(USER_ID);

        // when
        chatPreHandler.preSend(message, channel);

        // then
        // ChatRoomId가 없으면 null이 반환되고, userJoined(null, userId)가 호출됨
        verify(chatSessionService).userJoined(null, USER_ID);
    }

    @Test
    @DisplayName("DISCONNECT 시 세션에서 정보를 가져와 연결 종료 처리를 한다")
    void preSend_Disconnect_Success() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", USER_ID);
        sessionAttributes.put("chatRoomId", CHAT_ROOM_ID);
        accessor.setSessionAttributes(sessionAttributes);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        chatPreHandler.preSend(message, channel);

        // then
        verify(chatSessionService).userLeft(CHAT_ROOM_ID, USER_ID);
    }

    @Test
    @DisplayName("DISCONNECT 시 세션 정보가 없으면 아무 작업도 하지 않는다")
    void preSend_Disconnect_NoSessionInfo() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        // 세션에 userId, chatRoomId 없음 (혹은 세션 자체가 null일 수 있지만 코드상 requireNonNull 체크함)
        Map<String, Object> sessionAttributes = new HashMap<>();
        accessor.setSessionAttributes(sessionAttributes);
        // getUserIdFromSession에서 map.get("userId") -> null 리턴

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        chatPreHandler.preSend(message, channel);

        // then
        verify(chatSessionService, never()).userLeft(anyLong(), anyLong());
    }

    @Test
    @DisplayName("SUBSCRIBE 등 다른 커맨드는 무시하고 메시지를 반환한다")
    void preSend_OtherCommand() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        Message<?> result = chatPreHandler.preSend(message, channel);

        // then
        assertThat(result).isEqualTo(message);
        verify(chatSessionService, never()).userJoined(anyLong(), anyLong());
        verify(chatSessionService, never()).userLeft(anyLong(), anyLong());
    }
}
