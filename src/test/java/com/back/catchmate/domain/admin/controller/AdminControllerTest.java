package com.back.catchmate.domain.admin.controller;

import com.back.catchmate.domain.admin.dto.AdminRequest.AnswerInquiryRequest;
import com.back.catchmate.domain.admin.dto.AdminRequest.CreateNoticeRequest;
import com.back.catchmate.domain.admin.dto.AdminRequest.UpdateNoticeRequest;
import com.back.catchmate.domain.admin.dto.AdminResponse.*;
import com.back.catchmate.domain.admin.service.AdminService;
import com.back.catchmate.domain.board.dto.BoardResponse;
import com.back.catchmate.domain.board.service.BoardService;
import com.back.catchmate.domain.board.service.BookMarkService;
import com.back.catchmate.domain.chat.service.ChatRoomService;
import com.back.catchmate.domain.chat.service.ChatService;
import com.back.catchmate.domain.chat.service.UserChatRoomService;
import com.back.catchmate.domain.club.service.ClubService;
import com.back.catchmate.domain.enroll.service.EnrollService;
import com.back.catchmate.domain.inquiry.service.InquiryService;
import com.back.catchmate.domain.notice.service.NoticeService;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.domain.report.service.ReportService;
import com.back.catchmate.domain.user.service.BlockedUserService;
import com.back.catchmate.domain.user.service.UserService;
import com.back.catchmate.global.auth.service.AuthServiceImpl;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // --- 모든 컨트롤러 의존성 Mocking (컨텍스트 로딩 안정화용) ---
    @MockBean private AdminService adminService;
    @MockBean private JwtService jwtService;
    @MockBean private BlockedUserService blockedUserService;
    @MockBean private BoardService boardService;
    @MockBean private BookMarkService bookMarkService;
    @MockBean private ClubService clubService;
    @MockBean private EnrollService enrollService;
    @MockBean private NotificationService notificationService;
    @MockBean private UserService userService;
    @MockBean private InquiryService inquiryService;
    @MockBean private NoticeService noticeService;
    @MockBean private ReportService reportService;
    @MockBean private UserChatRoomService userChatRoomService;
    @MockBean private ChatService chatService;
    @MockBean private ChatRoomService chatRoomService;
    @MockBean private AuthServiceImpl authService; // AuthController 의존성

    private final String BASE_URI = "/admin";

    @BeforeEach
    void setUp() {
        // @JwtValidation에 의해 userId 1L 반환 Mock 설정
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    // --- 1. 통계 및 대시보드 API ---

    @Test
    @DisplayName("GET /dashboard - 대시보드 통계 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getDashboardStats_Success() throws Exception {
        AdminDashboardInfo response = AdminDashboardInfo.builder().totalUserCount(100L).build();
        given(adminService.getDashboardStats()).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/dashboard").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUserCount").value(100L));
    }

    @Test
    @DisplayName("GET /user/gender-ratio - 성비 통계 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getGenderRatio_Success() throws Exception {
        GenderRatioDto response = GenderRatioDto.builder().maleRatio(60.0).femaleRatio(40.0).build();
        given(adminService.getGenderRatio()).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/user/gender-ratio").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maleRatio").value(60.0));
    }

    @Test
    @DisplayName("GET /user/team-support - 구단별 응원자 통계 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getTeamSupportStats_Success() throws Exception {
        TeamSupportStatsInfo response = TeamSupportStatsInfo.builder().teamSupportCountMap(Map.of(1L, 5L)).build();
        given(adminService.getTeamSupportStats()).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/user/team-support").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamSupportCountMap").isMap());
    }

    @Test
    @DisplayName("GET /user/cheer-style - 응원 스타일별 가입자 수 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getCheerStyleStats_Success() throws Exception {
        CheerStyleStatsInfo response = CheerStyleStatsInfo.builder().cheerStyleCountMap(Map.of("감독", 10L)).build();
        given(adminService.getCheerStyleStats()).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/user/cheer-style").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cheerStyleCountMap").isMap());
    }

    // --- 2. 유저 및 게시글 조회 API ---

    @Test
    @DisplayName("GET /user - 유저 리스트 조회 성공 (클럽 필터)")
    @WithMockUser(roles = "ADMIN")
    void getUserInfoList_Success() throws Exception {
        PagedUserInfo response = PagedUserInfo.builder().totalElements(10L).build();
        given(adminService.getUserInfoList(eq("KIA"), any(Pageable.class))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/user")
                        .param("clubName", "KIA")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10L));
    }

    @Test
    @DisplayName("GET /user/{userId} - 유저 상세정보 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getUserInfo_Success() throws Exception {
        UserInfo response = UserInfo.builder().userId(10L).nickName("TargetUser").build();
        given(adminService.getUserInfo(eq(10L))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/user/{userId}", 10L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickName").value("TargetUser"));
    }

    @Test
    @DisplayName("GET /user/{userId}/board - 특정 유저의 게시글 리스트 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getBoardInfoList_Success() throws Exception {
        PagedBoardInfo response = PagedBoardInfo.builder().totalElements(3L).build();
        given(adminService.getBoardInfoList(eq(10L), any(Pageable.class))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/user/{userId}/board", 10L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3L));
    }

    @Test
    @DisplayName("GET /board/{boardId} - 특정 게시글 상세정보 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getBoardInfo_Success() throws Exception {
        BoardInfo response = BoardInfo.builder().boardId(10L).title("관리자 조회").build();
        given(adminService.getBoardInfo(eq(10L))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/board/{boardId}", 10L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("관리자 조회"));
    }

    // --- 3. 문의 관리 API ---

    @Test
    @DisplayName("GET /inquiry - 문의 내역 목록 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getInquiryList_Success() throws Exception {
        PagedInquiryInfo response = PagedInquiryInfo.builder().totalElements(5L).build();
        given(adminService.getInquiryList(any(Pageable.class))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/inquiry").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5L));
    }

    @Test
    @DisplayName("GET /inquiry/{inquiryId} - 문의 내역 단일 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getInquiry_Success() throws Exception {
        InquiryInfo response = InquiryInfo.builder().inquiryId(5L).content("문의 내용").build();
        given(adminService.getInquiry(eq(5L))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/inquiry/{inquiryId}", 5L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("문의 내용"));
    }

    @Test
    @DisplayName("PATCH /inquiry/{inquiryId}/answer - 문의 답변 작성 성공")
    @WithMockUser(roles = "ADMIN")
    void answerInquiry_Success() throws Exception {
        Long inquiryId = 5L;
        AnswerInquiryRequest request = new AnswerInquiryRequest("답변 내용");
        StateResponse response = new StateResponse(true);

        given(adminService.answerInquiry(any(), eq(inquiryId), any())).willReturn(response);

        mockMvc.perform(patch(BASE_URI + "/inquiry/{inquiryId}/answer", inquiryId)
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    // --- 4. 신고 관리 API ---

    @Test
    @DisplayName("GET /report - 신고 내역 목록 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getReportList_Success() throws Exception {
        PagedReportInfo response = PagedReportInfo.builder().totalElements(3L).build();
        given(adminService.getReportList(any(Pageable.class))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/report").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3L));
    }

    @Test
    @DisplayName("GET /report/{reportId} - 신고 내역 단일 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getReport_Success() throws Exception {
        ReportInfo response = ReportInfo.builder().reportId(1L).content("신고 내용").build();
        given(adminService.getReport(eq(1L))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/report/{reportId}", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("신고 내용"));
    }

    @Test
    @DisplayName("PATCH /report/{reportId}/process - 신고 처리 성공")
    @WithMockUser(roles = "ADMIN")
    void processReport_Success() throws Exception {
        StateResponse response = new StateResponse(true);
        given(adminService.processReport(eq(1L))).willReturn(response);

        mockMvc.perform(patch(BASE_URI + "/report/{reportId}/process", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    // --- 5. 공지사항 관리 API ---

    @Test
    @DisplayName("POST /notice - 공지글 등록 성공")
    @WithMockUser(roles = "ADMIN")
    void createNotice_Success() throws Exception {
        CreateNoticeRequest request = CreateNoticeRequest.builder().title("공지").content("내용").build();
        NoticeInfo response = NoticeInfo.builder().noticeId(1L).title("공지").build();

        given(adminService.createNotice(any(), any())).willReturn(response);

        mockMvc.perform(post(BASE_URI + "/notice")
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("공지"));
    }

    @Test
    @DisplayName("GET /notice/list - 공지사항 목록 조회 성공 (날짜 필터 없음)")
    @WithMockUser(roles = "ADMIN")
    void getNoticeList_Success() throws Exception {
        PagedNoticeInfo response = PagedNoticeInfo.builder().totalElements(7L).build();
        // LocalDate 파라미터는 null 대신 isNull()로 매칭해야 함
        given(adminService.getNoticeList(isNull(), isNull(), any(Pageable.class))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/notice/list").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(7L));
    }

    @Test
    @DisplayName("GET /notice/{noticeId} - 공지사항 단일 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getNotice_Success() throws Exception {
        NoticeInfo response = NoticeInfo.builder().noticeId(1L).title("단일 공지").build();
        given(adminService.getNotice(eq(1L))).willReturn(response);

        mockMvc.perform(get(BASE_URI + "/notice/{noticeId}", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("단일 공지"));
    }

    @Test
    @DisplayName("PUT /notice/{noticeId} - 공지사항 수정 성공")
    @WithMockUser(roles = "ADMIN")
    void updateNotice_Success() throws Exception {
        UpdateNoticeRequest request = UpdateNoticeRequest.builder().title("수정됨").content("내용").build();
        NoticeInfo response = NoticeInfo.builder().noticeId(1L).title("수정됨").build();

        given(adminService.updateNotice(any(), eq(1L), any())).willReturn(response);

        mockMvc.perform(put(BASE_URI + "/notice/{noticeId}", 1L)
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정됨"));
    }

    @Test
    @DisplayName("DELETE /notice/{noticeId} - 공지사항 삭제 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteNotice_Success() throws Exception {
        StateResponse response = new StateResponse(true);
        given(adminService.deleteNotice(any(), eq(1L))).willReturn(response);

        mockMvc.perform(delete(BASE_URI + "/notice/{noticeId}", 1L)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }
}
