package com.back.catchmate.domain.notification.controller;

import com.back.catchmate.domain.notification.dto.NotificationResponse.NotificationInfo;
import com.back.catchmate.domain.notification.dto.NotificationResponse.PagedNotificationInfo;
import com.back.catchmate.domain.notification.service.NotificationService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtService jwtService;

    // [중요] 모든 테스트 전에 토큰 파싱 로직을 Stubbing 합니다.
    @BeforeEach
    void setUp() {
        // "AccessToken" 헤더가 들어오면 1L (userId)을 반환하도록 설정
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    @Test
    @DisplayName("내가 받은 알림 목록 조회 API 테스트")
    @WithMockUser
    void getNotificationList_Success() throws Exception {
        // given
        PagedNotificationInfo response = PagedNotificationInfo.builder()
                .notificationInfoList(List.of(
                        NotificationInfo.builder().notificationId(1L).title("알림1").build(),
                        NotificationInfo.builder().notificationId(2L).title("알림2").build()
                ))
                .totalElements(2L)
                .totalPages(1)
                .build();

        given(notificationService.getNotificationList(any(), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/notifications/receive")
                        .header("AccessToken", "test-token") // [필수] 헤더 추가
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationInfoList").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("내가 받은 알림 단일 조회 API 테스트")
    @WithMockUser
    void getNotification_Success() throws Exception {
        // given
        Long notificationId = 1L;
        NotificationInfo response = NotificationInfo.builder()
                .notificationId(notificationId)
                .title("단일 알림")
                .body("내용")
                .createdAt(LocalDateTime.now())
                .build();

        given(notificationService.getNotification(any(), eq(notificationId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/notifications/receive/{notificationId}", notificationId)
                        .header("AccessToken", "test-token") // [필수] 헤더 추가
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notificationId))
                .andExpect(jsonPath("$.title").value("단일 알림"));
    }

    @Test
    @DisplayName("내가 받은 알림 삭제 API 테스트")
    @WithMockUser
    void deleteNotification_Success() throws Exception {
        // given
        Long notificationId = 1L;
        StateResponse response = new StateResponse(true);

        given(notificationService.deleteNotification(any(), eq(notificationId))).willReturn(response);

        // when & then
        mockMvc.perform(delete("/notifications/receive/{notificationId}", notificationId)
                        .header("AccessToken", "test-token") // [필수] 헤더 추가
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }
}
