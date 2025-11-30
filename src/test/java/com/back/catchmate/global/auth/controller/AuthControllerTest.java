package com.back.catchmate.global.auth.controller;

import com.back.catchmate.global.auth.dto.AuthRequest.LoginRequest;
import com.back.catchmate.global.auth.dto.AuthResponse.AuthInfo;
import com.back.catchmate.global.auth.dto.AuthResponse.NicknameCheckInfo;
import com.back.catchmate.global.auth.dto.AuthResponse.ReissueInfo;
import com.back.catchmate.global.auth.service.AuthServiceImpl;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthServiceImpl authService;

    @MockBean
    private JwtService jwtService; // WebConfig 초기화를 위한 Mock

    @Test
    @DisplayName("로그인 API 테스트 - 성공 시 토큰을 반환한다")
    @WithMockUser
    void login_Success() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("test@test.com")
                .providerId("12345")
                .provider("google")
                .fcmToken("fcm_token")
                .build();

        AuthInfo response = AuthInfo.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .isFirstLogin(false)
                .build();

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"))
                .andExpect(jsonPath("$.isFirstLogin").value(false));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 필수값 누락 시 400 에러 발생")
    @WithMockUser
    void login_Fail_Validation() throws Exception {
        // given
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("") // 빈 값
                .provider(null) // null
                .build();

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("닉네임 중복 확인 API 테스트")
    @WithMockUser
    void checkNickname_Success() throws Exception {
        // given
        String nickName = "TestUser";
        NicknameCheckInfo response = new NicknameCheckInfo(true); // 사용 가능

        given(authService.checkNickname(eq(nickName))).willReturn(response);

        // when & then
        mockMvc.perform(get("/auth/check-nickname")
                        .param("nickName", nickName)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("토큰 재발급 API 테스트")
    @WithMockUser
    void reissue_Success() throws Exception {
        // given
        String refreshToken = "valid_refresh_token";
        ReissueInfo response = new ReissueInfo("new_access_token");

        given(authService.reissue(eq(refreshToken))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/reissue")
                        .header("RefreshToken", refreshToken)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access_token"));
    }

    @Test
    @DisplayName("로그아웃 API 테스트")
    @WithMockUser
    void logout_Success() throws Exception {
        // given
        String refreshToken = "valid_refresh_token";
        StateResponse response = new StateResponse(true);

        given(authService.logout(eq(refreshToken))).willReturn(response);

        // when & then
        mockMvc.perform(delete("/auth/logout")
                        .header("RefreshToken", refreshToken)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }
}
