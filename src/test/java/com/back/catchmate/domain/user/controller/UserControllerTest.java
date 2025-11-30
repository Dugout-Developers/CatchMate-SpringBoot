package com.back.catchmate.domain.user.controller;

import com.back.catchmate.domain.user.dto.UserRequest.UserJoinRequest;
import com.back.catchmate.domain.user.dto.UserRequest.UserProfileUpdateRequest;
import com.back.catchmate.domain.user.dto.UserResponse.LoginInfo;
import com.back.catchmate.domain.user.dto.UserResponse.PagedUserInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UnreadStatusInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UpdateAlarmInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.AlarmType;
import com.back.catchmate.domain.user.service.BlockedUserService;
import com.back.catchmate.domain.user.service.UserService;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;
    @MockBean private BlockedUserService blockedUserService;
    @MockBean private JwtService jwtService; // ArgumentResolver용

    @BeforeEach
    void setUp() {
        // @JwtValidation이 붙은 파라미터 처리를 위해 항상 1L 반환 설정
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    @Test
    @DisplayName("추가 정보 입력(회원가입) API 테스트")
    @WithMockUser
    void addProfile_Success() throws Exception {
        // given
        UserJoinRequest request = UserJoinRequest.builder()
                .email("test@test.com")
                .provider("google")
                .providerId("12345")
                .nickName("NewUser")
                .gender('M')
                .birthDate(LocalDate.now())
                .profileImageUrl("http://image.url/profile.jpg")
                .fcmToken("fcm_token")
                .favoriteClubId(1L)
                .watchStyle("style")
                .build();

        LoginInfo response = LoginInfo.builder()
                .userId(1L)
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        given(userService.joinUser(any(UserJoinRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/users/additional-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    @DisplayName("나의 정보 조회 API 테스트")
    @WithMockUser
    void getMyProfile_Success() throws Exception {
        // given
        UserInfo response = UserInfo.builder().userId(1L).nickName("MyNick").build();
        given(userService.getMyProfile(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/users/profile")
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickName").value("MyNick"));
    }

    @Test
    @DisplayName("다른 유저 정보 조회 API 테스트")
    @WithMockUser
    void getOtherUserProfile_Success() throws Exception {
        // given
        Long targetUserId = 2L;
        UserInfo response = UserInfo.builder().userId(targetUserId).nickName("OtherNick").build();
        given(userService.getOtherUserProfile(anyLong(), eq(targetUserId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/users/profile/{profileUserId}", targetUserId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(targetUserId));
    }

    @Test
    @DisplayName("읽지 않은 채팅/알림 여부 조회 API 테스트")
    @WithMockUser
    void hasUnreadMessagesOrNotifications_Success() throws Exception {
        // given
        UnreadStatusInfo response = UnreadStatusInfo.builder()
                .hasUnreadChat(true)
                .hasUnreadNotification(false)
                .build();
        given(userService.hasUnreadMessagesOrNotifications(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/users/has-unread")
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasUnreadChat").value(true));
    }

    @Test
    @DisplayName("나의 정보 수정 API 테스트 (Multipart + PATCH)")
    @WithMockUser
    void updateProfile_Success() throws Exception {
        // given
        UserProfileUpdateRequest requestDto = UserProfileUpdateRequest.builder()
                .nickName("UpdatedNick")
                .favoriteClubId(1L)
                .watchStyle("newStyle")
                .build();

        // JSON Part
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8)
        );

        // File Part
        MockMultipartFile imagePart = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "data".getBytes()
        );

        StateResponse response = new StateResponse(true);
        given(userService.updateProfile(any(), any(), anyLong())).willReturn(response);

        // when & then
        // MockMvc는 기본적으로 Multipart 요청을 POST로 처리함. PATCH로 보내려면 with(request -> ...) 사용 필요
        mockMvc.perform(multipart(HttpMethod.PATCH, "/users/profile")
                        .file(requestPart)
                        .file(imagePart)
                        .header("AccessToken", "token")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("알림 설정 API 테스트")
    @WithMockUser
    void updateAlarm_Success() throws Exception {
        // given
        UpdateAlarmInfo response = UpdateAlarmInfo.builder()
                .userId(1L)
                .alarmType(AlarmType.CHAT)
                .isEnabled('Y')
                .build();

        given(userService.updateAlarm(anyLong(), eq(AlarmType.CHAT), eq('Y'))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/users/alarm")
                        .header("AccessToken", "token")
                        .param("alarmType", "CHAT")
                        .param("isEnabled", "Y")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alarmType").value("CHAT"))
                .andExpect(jsonPath("$.isEnabled").value("Y"));
    }

    @Test
    @DisplayName("유저 탈퇴 API 테스트")
    @WithMockUser
    void deleteUser_Success() throws Exception {
        // given
        StateResponse response = new StateResponse(true);
        given(userService.deleteUser(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(delete("/users/withdraw")
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("유저 차단 API 테스트")
    @WithMockUser
    void blockUser_Success() throws Exception {
        // given
        Long blockedUserId = 2L;
        StateResponse response = new StateResponse(true);
        given(blockedUserService.blockUser(anyLong(), eq(blockedUserId))).willReturn(response);

        // when & then
        mockMvc.perform(post("/users/block/{blockedUserId}", blockedUserId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("유저 차단 해제 API 테스트")
    @WithMockUser
    void unblockUser_Success() throws Exception {
        // given
        Long blockedUserId = 2L;
        StateResponse response = new StateResponse(true);
        given(blockedUserService.unblockUser(anyLong(), eq(blockedUserId))).willReturn(response);

        // when & then
        mockMvc.perform(delete("/users/block/{blockedUserId}", blockedUserId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("차단한 유저 목록 조회 API 테스트")
    @WithMockUser
    void getBlockedUserList_Success() throws Exception {
        // given
        PagedUserInfo response = PagedUserInfo.builder().totalElements(5L).build();
        given(blockedUserService.getBlockedUserList(anyLong(), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/users/block")
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5L));
    }
}
