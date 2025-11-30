package com.back.catchmate.domain.notice.controller;

import com.back.catchmate.domain.notice.dto.NoticeResponse.NoticeInfo;
import com.back.catchmate.domain.notice.dto.NoticeResponse.PagedNoticeInfo;
import com.back.catchmate.domain.notice.service.NoticeService;
import com.back.catchmate.global.jwt.JwtService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeService noticeService;

    @MockBean
    private JwtService jwtService; // WebConfig 로딩을 위해 필요

    @Test
    @DisplayName("공지사항 목록 조회 API 테스트")
    @WithMockUser
    void getNoticeList_Success() throws Exception {
        // given
        NoticeInfo notice1 = NoticeInfo.builder()
                .noticeId(1L)
                .title("공지1")
                .build();
        NoticeInfo notice2 = NoticeInfo.builder()
                .noticeId(2L)
                .title("공지2")
                .build();

        PagedNoticeInfo response = PagedNoticeInfo.builder()
                .noticeInfoList(List.of(notice1, notice2))
                .totalElements(2L)
                .totalPages(1)
                .build();

        given(noticeService.getNoticeList(any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/notices/list")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeInfoList").isArray())
                .andExpect(jsonPath("$.noticeInfoList[0].title").value("공지1"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("공지사항 단일 조회 API 테스트")
    @WithMockUser
    void getNotice_Success() throws Exception {
        // given
        Long noticeId = 1L;
        NoticeInfo response = NoticeInfo.builder()
                .noticeId(noticeId)
                .title("공지 제목")
                .content("공지 내용")
                .createdAt(LocalDateTime.now())
                .build();

        given(noticeService.getNotice(eq(noticeId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/notices/{noticeId}", noticeId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeId").value(noticeId))
                .andExpect(jsonPath("$.title").value("공지 제목"))
                .andExpect(jsonPath("$.content").value("공지 내용"));
    }
}
