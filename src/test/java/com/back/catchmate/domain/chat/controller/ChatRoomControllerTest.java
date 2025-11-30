package com.back.catchmate.domain.chat.controller;

import com.back.catchmate.domain.chat.dto.ChatResponse.ChatRoomInfo;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatRoomInfo;
import com.back.catchmate.domain.chat.service.ChatRoomService;
import com.back.catchmate.domain.chat.service.UserChatRoomService;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfoList;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatRoomService chatRoomService;

    @MockBean
    private UserChatRoomService userChatRoomService;

    @MockBean
    private JwtService jwtService; // ArgumentResolver용 Mock

    @BeforeEach
    void setUp() {
        // JwtValidation 동작을 위한 Mock 설정
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    @Test
    @DisplayName("내가 속한 채팅방 목록 조회 API 테스트")
    @WithMockUser
    void getChatRoomList_Success() throws Exception {
        // given
        PagedChatRoomInfo response = PagedChatRoomInfo.builder()
                .totalElements(5L)
                .build();

        given(chatRoomService.getChatRoomList(any(), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/chat-rooms/list")
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5L));
    }

    @Test
    @DisplayName("채팅방 정보 조회 API 테스트")
    @WithMockUser
    void getChatRoom_Success() throws Exception {
        // given
        Long chatRoomId = 10L;
        ChatRoomInfo response = ChatRoomInfo.builder()
                .chatRoomId(chatRoomId)
                .build();

        given(chatRoomService.getChatRoom(any(), eq(chatRoomId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/chat-rooms/{chatRoomId}", chatRoomId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomId").value(chatRoomId));
    }

    @Test
    @DisplayName("채팅방 나가기 API 테스트")
    @WithMockUser
    void leaveChatRoom_Success() throws Exception {
        // given
        Long chatRoomId = 10L;
        StateResponse response = new StateResponse(true);

        given(chatRoomService.leaveChatRoom(any(), eq(chatRoomId))).willReturn(response);

        // when & then
        mockMvc.perform(delete("/chat-rooms/{chatRoomId}", chatRoomId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("채팅방 참여 유저 리스트 조회 API 테스트")
    @WithMockUser
    void getUsersInChatRoom_Success() throws Exception {
        // given
        Long chatRoomId = 10L;
        UserInfoList response = UserInfoList.builder()
                .userInfoList(List.of(
                        UserInfo.builder().userId(1L).nickName("User1").build(),
                        UserInfo.builder().userId(2L).nickName("User2").build()
                ))
                .build();

        given(userChatRoomService.getUserInfoList(any(), eq(chatRoomId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/chat-rooms/{chatRoomId}/user-list", chatRoomId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userInfoList").isArray())
                .andExpect(jsonPath("$.userInfoList[0].nickName").value("User1"));
    }

    @Test
    @DisplayName("채팅방 이미지 변경 API 테스트 (Multipart + PATCH)")
    @WithMockUser
    void updateChatRoomImage_Success() throws Exception {
        // given
        Long chatRoomId = 10L;
        MockMultipartFile image = new MockMultipartFile(
                "chatRoomImage", "test.jpg", "image/jpeg", "data".getBytes());
        StateResponse response = new StateResponse(true);

        given(chatRoomService.updateChatRoomImage(any(), eq(chatRoomId), any())).willReturn(response);

        // when & then
        // multipart()는 기본적으로 POST이므로 HttpMethod.PATCH를 명시해야 함
        mockMvc.perform(multipart(HttpMethod.PATCH, "/chat-rooms/{chatRoomId}/image", chatRoomId)
                        .file(image)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("유저 강제 퇴장 API 테스트")
    @WithMockUser
    void kickUserFromChatRoom_Success() throws Exception {
        // given
        Long chatRoomId = 10L;
        Long targetUserId = 20L;
        StateResponse response = new StateResponse(true);

        given(chatRoomService.kickUserFromChatRoom(any(), eq(chatRoomId), eq(targetUserId))).willReturn(response);

        // when & then
        mockMvc.perform(delete("/chat-rooms/{chatRoomId}/users/{userId}", chatRoomId, targetUserId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("채팅방 알림 설정 API 테스트")
    @WithMockUser
    void updateNotificationSetting_Success() throws Exception {
        // given
        Long chatRoomId = 10L;
        boolean enable = false;
        StateResponse response = new StateResponse(true);

        given(chatRoomService.updateNotificationSetting(any(), eq(chatRoomId), eq(enable))).willReturn(response);

        // when & then
        mockMvc.perform(put("/chat-rooms/{chatRoomId}/notification", chatRoomId)
                        .header("AccessToken", "token")
                        .param("enable", String.valueOf(enable))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }
}
