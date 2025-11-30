package com.back.catchmate.domain.inquiry.controller;

import com.back.catchmate.domain.inquiry.dto.InquiryRequest.CreateInquiryRequest;
import com.back.catchmate.domain.inquiry.dto.InquiryResponse.InquiryInfo;
import com.back.catchmate.domain.inquiry.entity.InquiryType;
import com.back.catchmate.domain.inquiry.service.InquiryService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InquiryService inquiryService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // JwtValidation 동작을 위한 Mock 설정
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    @Test
    @DisplayName("고객센터 문의 등록 API 테스트")
    @WithMockUser
    void submitInquiry_Success() throws Exception {
        // given
        CreateInquiryRequest request = new CreateInquiryRequest(InquiryType.ACCOUNT, "로그인이 안돼요.");
        StateResponse response = new StateResponse(true);

        given(inquiryService.submitInquiry(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/inquiries")
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("고객센터 문의 등록 시 필수값 누락은 400 예외를 발생시킨다")
    @WithMockUser
    void submitInquiry_Fail_Validation() throws Exception {
        // given
        CreateInquiryRequest invalidRequest = new CreateInquiryRequest(null, ""); // 타입 null, 내용 빈 문자열

        // when & then
        mockMvc.perform(post("/inquiries")
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("고객센터 문의 조회 API 테스트")
    @WithMockUser
    void getInquiry_Success() throws Exception {
        // given
        Long inquiryId = 10L;
        InquiryInfo response = InquiryInfo.builder()
                .inquiryId(inquiryId)
                .inquiryType(InquiryType.ACCOUNT)
                .content("로그인이 안돼요.")
                .nickName("Tester")
                .createdAt(LocalDateTime.now())
                .build();

        given(inquiryService.getInquiry(eq(inquiryId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/inquiries/{inquiryId}", inquiryId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inquiryId").value(inquiryId))
                .andExpect(jsonPath("$.content").value("로그인이 안돼요."))
                .andExpect(jsonPath("$.inquiryType").value("ACCOUNT"));
    }
}
