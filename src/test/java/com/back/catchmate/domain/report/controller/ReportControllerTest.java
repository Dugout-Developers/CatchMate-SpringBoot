package com.back.catchmate.domain.report.controller;

import com.back.catchmate.domain.report.dto.ReportRequest.CreateReportRequest;
import com.back.catchmate.domain.report.entity.ReportType;
import com.back.catchmate.domain.report.service.ReportService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // JwtValidation 리졸버 동작을 위한 Mock
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    @Test
    @DisplayName("유저 신고 API 테스트 - 성공")
    @WithMockUser
    void reportUser_Success() throws Exception {
        // given
        Long reportedUserId = 100L;
        CreateReportRequest request = CreateReportRequest.builder()
                .reportType(ReportType.PROFANITY)
                .content("욕설을 사용했습니다.")
                .build();

        StateResponse response = new StateResponse(true);

        given(reportService.reportUser(any(), eq(reportedUserId), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/reports/{reportedUserId}", reportedUserId)
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("유저 신고 시 필수값 누락은 400 예외를 발생시킨다")
    @WithMockUser
    void reportUser_Fail_Validation() throws Exception {
        // given
        Long reportedUserId = 100L;
        // 필수값인 reportType과 content 누락 (DTO 검증 조건에 따라 다름)
        CreateReportRequest invalidRequest = CreateReportRequest.builder()
                .reportType(null)
                .content("")
                .build();

        // when & then
        mockMvc.perform(post("/reports/{reportedUserId}", reportedUserId)
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
