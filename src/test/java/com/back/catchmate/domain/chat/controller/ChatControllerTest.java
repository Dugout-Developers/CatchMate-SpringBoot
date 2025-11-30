package com.back.catchmate.domain.chat.controller;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatRequest.ReadChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.ChatMessageInfo;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatMessageInfo;
import com.back.catchmate.domain.chat.service.ChatService;
import com.back.catchmate.global.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatController chatController; // WebSocket 메서드 직접 호출용

    @MockBean
    private ChatService chatService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // JwtValidation 동작을 위한 Mock
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    // 1. HTTP GET 메서드 테스트
    @Test
    @DisplayName("채팅 내역 조회 API 테스트")
    @WithMockUser
    void findChatMessageList_Success() throws Exception {
        // given
        Long chatRoomId = 100L;
        ChatMessageInfo messageInfo = ChatMessageInfo.builder()
                .chatMessageId("msg-1")
                .roomId(chatRoomId)
                .senderId(1L)
                .content("안녕하세요")
                .messageType(MessageType.TALK) // [수정] DTO 필드에 맞춤
                .build();

        PagedChatMessageInfo response = PagedChatMessageInfo.builder()
                .chatMessageInfoList(List.of(messageInfo))
                .isLast(true) // [수정] hasNext -> isLast
                .isFirst(true)
                .lastMessageId("msg-1")
                .build();

        given(chatService.getChatMessageList(anyLong(), eq(chatRoomId), any(), anyInt()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/chats/{chatRoomId}", chatRoomId)
                        .header("AccessToken", "token")
                        .param("lastMessageId", "msg-0") // 선택 파라미터
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatMessageInfoList").isArray())
                .andExpect(jsonPath("$.chatMessageInfoList[0].content").value("안녕하세요"))
                .andExpect(jsonPath("$.isLast").value(true)); // [수정] 필드명 변경 확인
    }

    // 2. WebSocket 메서드 테스트 (직접 호출)
    @Test
    @DisplayName("메시지 전송 (WebSocket) - 서비스 호출 검증")
    void sendMessage_Success() throws Exception {
        // given
        Long chatRoomId = 100L;
        ChatMessageRequest request = new ChatMessageRequest();
        // request setter나 builder가 있다면 값 설정 (여기선 로직 검증 위주)

        // when
        // @MessageMapping 메서드는 MockMvc로 호출 불가하므로 직접 호출
        chatController.sendMessage(chatRoomId, request);

        // then
        verify(chatService).sendChatMessage(eq(chatRoomId), eq(request));
    }

    @Test
    @DisplayName("읽음 처리 (WebSocket) - 서비스 호출 검증")
    void updateLastReadTime_Success() {
        // given
        ReadChatMessageRequest request = new ReadChatMessageRequest();

        // when
        chatController.updateLastReadTime(request);

        // then
        verify(chatService).updateLastReadTime(eq(request));
    }
}
