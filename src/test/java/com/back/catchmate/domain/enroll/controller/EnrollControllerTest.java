package com.back.catchmate.domain.enroll.controller;

import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.*;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.service.EnrollService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
class EnrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollService enrollService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // @JwtValidation 리졸버가 토큰을 파싱할 때 항상 1L을 반환하도록 설정
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    @Test
    @DisplayName("직관 신청 API 테스트")
    @WithMockUser
    void requestEnroll_Success() throws Exception {
        // given
        Long boardId = 1L;
        CreateEnrollRequest request = CreateEnrollRequest.builder()
                .description("신청합니다")
                .build();

        CreateEnrollInfo response = CreateEnrollInfo.builder()
                .enrollId(100L)
                .requestAt(LocalDateTime.now())
                .build();

        given(enrollService.requestEnroll(any(), eq(boardId), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/enrolls/{boardId}", boardId)
                        .header("AccessToken", "test-token") // 헤더 필수
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrollId").value(100L));
    }

    @Test
    @DisplayName("직관 신청 취소 API 테스트")
    @WithMockUser
    void cancelEnroll_Success() throws Exception {
        // given
        Long enrollId = 100L;
        CancelEnrollInfo response = CancelEnrollInfo.builder()
                .enrollId(enrollId)
                .deletedAt(LocalDateTime.now())
                .build();

        given(enrollService.cancelEnroll(eq(enrollId), any())).willReturn(response);

        // when & then
        mockMvc.perform(delete("/enrolls/cancel/{enrollId}", enrollId)
                        .header("AccessToken", "test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrollId").value(enrollId));
    }

    @Test
    @DisplayName("내가 보낸 직관 신청 목록 조회 API 테스트")
    @WithMockUser
    void getRequestEnrollList_Success() throws Exception {
        // given
        PagedEnrollRequestInfo response = PagedEnrollRequestInfo.builder()
                .totalElements(5L)
                .totalPages(1)
                .build();

        given(enrollService.getRequestEnrollList(any(), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/enrolls/request")
                        .header("AccessToken", "test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5L));
    }

    @Test
    @DisplayName("내가 작성한 게시글에 대한 직관 신청 목록 전체 조회 API 테스트")
    @WithMockUser
    void getReceiveEnrollList_Success() throws Exception {
        // given
        PagedEnrollReceiveInfo response = PagedEnrollReceiveInfo.builder()
                .totalElements(10L)
                .build();

        given(enrollService.getReceiveEnrollList(any(), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/enrolls/receive/all")
                        .header("AccessToken", "test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10L));
    }

    @Test
    @DisplayName("특정 게시글에 대한 직관 신청 목록 조회 API 테스트")
    @WithMockUser
    void getReceiveEnrollListByBoardId_Success() throws Exception {
        // given
        Long boardId = 10L;
        PagedEnrollReceiveInfo response = PagedEnrollReceiveInfo.builder()
                .totalElements(3L)
                .build();

        given(enrollService.getReceiveEnrollListByBoardId(any(), eq(boardId), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/enrolls/receive")
                        .param("boardId", String.valueOf(boardId))
                        .header("AccessToken", "test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3L));
    }

    @Test
    @DisplayName("새로운 신청 개수 조회 API 테스트")
    @WithMockUser
    void getNewEnrollmentListCount_Success() throws Exception {
        // given
        NewEnrollCountInfo response = NewEnrollCountInfo.builder()
                .newEnrollCount(5)
                .build();

        given(enrollService.getNewEnrollListCount(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/enrolls/new-count")
                        .header("AccessToken", "test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newEnrollCount").value(5));
    }

    @Test
    @DisplayName("받은 직관 신청 수락 API 테스트")
    @WithMockUser
    void acceptEnroll_Success() throws Exception {
        // given
        Long enrollId = 100L;
        UpdateEnrollInfo response = UpdateEnrollInfo.builder()
                .enrollId(enrollId)
                .acceptStatus(AcceptStatus.ACCEPTED)
                .build();

        given(enrollService.acceptEnroll(eq(enrollId), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/enrolls/{enrollId}/accept", enrollId)
                        .header("AccessToken", "test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptStatus").value("ACCEPTED"));
    }

    @Test
    @DisplayName("받은 직관 신청 거절 API 테스트")
    @WithMockUser
    void rejectEnroll_Success() throws Exception {
        // given
        Long enrollId = 100L;
        UpdateEnrollInfo response = UpdateEnrollInfo.builder()
                .enrollId(enrollId)
                .acceptStatus(AcceptStatus.REJECTED)
                .build();

        given(enrollService.rejectEnroll(eq(enrollId), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/enrolls/{enrollId}/reject", enrollId)
                        .header("AccessToken", "test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptStatus").value("REJECTED"));
    }

    @Test
    @DisplayName("보낸 신청 상세 조회 API 테스트")
    @WithMockUser
    void getEnrollDescriptionById_Success() throws Exception {
        // given
        Long boardId = 50L;
        EnrollDescriptionInfo response = EnrollDescriptionInfo.builder()
                .enrollId(200L)
                .description("상세 내용")
                .build();

        given(enrollService.getEnrollDescriptionById(eq(boardId), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/enrolls/{boardId}/description", boardId)
                        .header("AccessToken", "test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("상세 내용"));
    }
}
