package com.back.catchmate.domain.admin.service;

import com.back.catchmate.domain.admin.dto.AdminRequest.AnswerInquiryRequest;
import com.back.catchmate.domain.admin.dto.AdminRequest.CreateNoticeRequest;
import com.back.catchmate.domain.admin.dto.AdminRequest.UpdateNoticeRequest;
import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.inquiry.entity.Inquiry;
import com.back.catchmate.domain.inquiry.entity.InquiryType;
import com.back.catchmate.domain.inquiry.repository.InquiryRepository;
import com.back.catchmate.domain.notice.entity.Notice;
import com.back.catchmate.domain.notice.repository.NoticeRepository;
import com.back.catchmate.domain.notification.service.FCMService;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.domain.report.entity.Report;
import com.back.catchmate.domain.report.entity.ReportType;
import com.back.catchmate.domain.report.repository.ReportRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.back.catchmate.domain.notification.message.NotificationMessages.INQUIRY_ANSWER_BODY;
import static com.back.catchmate.domain.notification.message.NotificationMessages.INQUIRY_ANSWER_TITLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AdminServiceImplTest {

    @Autowired private AdminService adminService;
    @Autowired private UserRepository userRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private InquiryRepository inquiryRepository;
    @Autowired private NoticeRepository noticeRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private UserChatRoomRepository userChatRoomRepository;
    @Autowired private EntityManager em;

    @MockBean private FCMService fcmService;
    @MockBean private NotificationService notificationService;

    private User adminUser;
    private User regularUser;
    private Club kiaClub;
    private Club hanhwaClub;

    @BeforeEach
    void setUp() {
        // 데이터 격리를 위한 초기화
        reportRepository.deleteAll();
        inquiryRepository.deleteAll();
        noticeRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
        gameRepository.deleteAll(); // Game도 삭제
        clubRepository.deleteAll();

        // [중요] KBO 팀 ID (1L~10L) 중 일부를 생성하여 통계 초기화 로직 검증
        kiaClub = createAndSaveClub(1L, "KIA Tigers", "Gwangju");
        hanhwaClub = createAndSaveClub(2L, "Hanwha Eagles", "Daejeon");

        createGame(kiaClub);

        adminUser = userRepository.save(createUser("admin@test.com", "Admin", kiaClub, Authority.ROLE_ADMIN, "감독"));
        regularUser = userRepository.save(createUser("user@test.com", "Regular", hanhwaClub, Authority.ROLE_USER, "먹보"));

        em.flush();
        em.clear();
    }

    // --- 1. 통계 및 대시보드 API ---

    @Test
    @DisplayName("대시보드 통계 조회 성공")
    void getDashboardStats_Success() {
        // given
        Board board = createAndSaveBoard(regularUser, hanhwaClub, true);
        createAndSaveReport(regularUser, adminUser);
        createAndSaveInquiry(regularUser);

        // when
        AdminResponse.AdminDashboardInfo stats = adminService.getDashboardStats();

        // then
        assertThat(stats.getTotalUserCount()).isEqualTo(2L); // Admin + Regular
        assertThat(stats.getTotalBoardCount()).isEqualTo(1L);
        assertThat(stats.getTotalReportCount()).isEqualTo(1L);
        assertThat(stats.getTotalInquiryCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("성비 통계 조회 성공")
    void getGenderRatio_Success() {
        // given
        userRepository.save(createUser("male2@test.com", "M2", hanhwaClub, Authority.ROLE_USER, "먹보", 'M'));
        userRepository.save(createUser("female@test.com", "F1", kiaClub, Authority.ROLE_USER, "보살", 'F'));

        // 현재 총 4명 (M=3, F=1)
        // M 비율: 3/4 = 75.0, F 비율: 1/4 = 25.0

        // when
        AdminResponse.GenderRatioDto ratio = adminService.getGenderRatio();

        // then
        assertThat(ratio.getMaleRatio()).isEqualTo(75.0);
        assertThat(ratio.getFemaleRatio()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("구단별 응원자 통계 조회 성공 - 0 초기화 검증")
    void getTeamSupportStats_Success() {
        // given
        // KIA: 1명 (admin), Hanwha: 1명 (regular)

        // when
        AdminResponse.TeamSupportStatsInfo stats = adminService.getTeamSupportStats();

        // then
        Map<Long, Long> map = stats.getTeamSupportCountMap();
        assertThat(map).containsKeys(1L, 2L, 3L, 12L); // KBO 팀 ID 전체 포함 확인

        assertThat(map.get(kiaClub.getId())).isEqualTo(1L); // ID 1L
        assertThat(map.get(hanhwaClub.getId())).isEqualTo(1L); // ID 2L
        assertThat(map.get(10L)).isEqualTo(0L); // DB에 없는 팀은 0으로 초기화되었는지 확인
    }

    @Test
    @DisplayName("응원 스타일별 가입자 수 조회 성공")
    void getCheerStyleStats_Success() {
        // given
        userRepository.save(createUser("m2@test.com", "M2", hanhwaClub, Authority.ROLE_USER, "먹보")); // 먹보 2명

        // when
        AdminResponse.CheerStyleStatsInfo stats = adminService.getCheerStyleStats();

        // then
        Map<String, Long> map = stats.getCheerStyleCountMap();
        assertThat(map).containsKeys("감독", "먹보", "보살"); // 정의된 역할 전체 포함 확인

        assertThat(map.get("감독")).isEqualTo(1L);
        assertThat(map.get("먹보")).isEqualTo(2L);
        assertThat(map.get("보살")).isEqualTo(0L); // 0으로 초기화되었는지 확인
    }

    // --- 2. 유저 및 게시글 관리 API ---

    @Test
    @DisplayName("유저 리스트 조회 성공 - 필터 없음")
    void getUserInfoList_NoFilter_Success() {
        AdminResponse.PagedUserInfo result = adminService.getUserInfoList(null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getUserInfoList().stream().map(AdminResponse.UserInfo::getNickName).collect(Collectors.toList())).containsExactlyInAnyOrder("Admin", "Regular");
    }

    @Test
    @DisplayName("유저 리스트 조회 성공 - 클럽 필터")
    void getUserInfoList_ByClubName_Success() {
        AdminResponse.PagedUserInfo result = adminService.getUserInfoList("KIA Tigers", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getUserInfoList().get(0).getNickName()).isEqualTo("Admin");
    }

    @Test
    @DisplayName("유저 상세정보 조회 성공")
    void getUserInfo_Success() {
        AdminResponse.UserInfo info = adminService.getUserInfo(adminUser.getId());
        assertThat(info.getNickName()).isEqualTo("Admin");
    }

    @Test
    @DisplayName("특정 유저의 게시글 리스트 조회 성공")
    void getBoardInfoList_Success() {
        createAndSaveBoard(regularUser, hanhwaClub, true);
        createAndSaveBoard(regularUser, hanhwaClub, true); // 두 개 게시글

        AdminResponse.PagedBoardInfo result = adminService.getBoardInfoList(regularUser.getId(), PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    // --- 3. 문의 관리 API ---

    @Test
    @DisplayName("문의 답변 작성 성공 - DB 업데이트 및 FCM/알림 서비스 호출 검증")
    void answerInquiry_Success() throws IOException {
        // given
        Inquiry inquiry = createAndSaveInquiry(regularUser);
        AnswerInquiryRequest request = new AnswerInquiryRequest("답변입니다");

        // when
        StateResponse response = adminService.answerInquiry(adminUser.getId(), inquiry.getId(), request);

        // then
        assertThat(response.isState()).isTrue();

        // 1. DB 상태 검증
        Inquiry answeredInquiry = inquiryRepository.findById(inquiry.getId()).orElseThrow();
        assertThat(answeredInquiry.getAnswer()).isEqualTo("답변입니다");
        assertThat(answeredInquiry.getIsCompleted()).isTrue();
        assertThat(answeredInquiry.getAnsweredBy().getId()).isEqualTo(adminUser.getId());

        // 2. 외부 호출 검증 (FCM, Notification)
        verify(fcmService).sendMessageByToken(
                eq(regularUser.getFcmToken()),
                eq(INQUIRY_ANSWER_TITLE),
                eq(INQUIRY_ANSWER_BODY),
                eq(inquiry.getId())
        );
        verify(notificationService).createNotification(
                eq(INQUIRY_ANSWER_TITLE),
                eq(INQUIRY_ANSWER_BODY),
                eq(null), // Inquiry 알림은 Sender가 null
                eq(inquiry.getId()),
                eq(regularUser.getId())
        );
    }

    // --- 4. 신고 관리 API ---

    @Test
    @DisplayName("신고 처리 성공 - DB 상태 업데이트 확인")
    void processReport_Success() {
        // given
        Report report = createAndSaveReport(regularUser, adminUser);
        assertThat(report.getIsProcessed()).isFalse();

        // when
        StateResponse response = adminService.processReport(report.getId());

        // then
        assertThat(response.isState()).isTrue();
        Report processedReport = reportRepository.findById(report.getId()).orElseThrow();
        assertThat(processedReport.getIsProcessed()).isTrue();
    }

    // --- 5. 공지사항 관리 API ---

    @Test
    @DisplayName("공지글 등록 성공")
    void createNotice_Success() {
        // given
        CreateNoticeRequest request = CreateNoticeRequest.builder().title("공지").content("내용").build();

        // when
        AdminResponse.NoticeInfo info = adminService.createNotice(adminUser.getId(), request);

        // then
        assertThat(info.getTitle()).isEqualTo("공지");
        assertThat(noticeRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("공지사항 수정 성공")
    void updateNotice_Success() {
        // given
        Notice notice = createAndSaveNotice(adminUser, "오래된 공지", "내용");
        UpdateNoticeRequest request = UpdateNoticeRequest.builder().title("수정된 제목").content("수정된 내용").build();

        // when
        adminService.updateNotice(adminUser.getId(), notice.getId(), request);

        // then
        Notice updatedNotice = noticeRepository.findById(notice.getId()).orElseThrow();
        assertThat(updatedNotice.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedNotice.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("공지사항 삭제 성공")
    void deleteNotice_Success() {
        // given
        Notice notice = createAndSaveNotice(adminUser, "삭제될 공지", "내용");

        em.flush();
        em.clear();

        // when
        StateResponse response = adminService.deleteNotice(adminUser.getId(), notice.getId());

        // [추가] 변경 사항 반영 및 캐시 초기화 (중요!)
        em.flush();
        em.clear();

        // then
        assertThat(response.isState()).isTrue();
        // Soft delete 확인 (이제 DB에서 새로 조회하므로 deletedAt이 적용된 상태여야 함)
        assertThat(noticeRepository.findByIdAndDeletedAtIsNull(notice.getId())).isEmpty();
    }

    // --- 예외 케이스 ---

    @Test
    @DisplayName("존재하지 않는 유저로 문의 답변 시도 시 예외 발생")
    void answerInquiry_Fail_UserNotFound() {
        // given
        Inquiry inquiry = createAndSaveInquiry(regularUser);
        AnswerInquiryRequest request = new AnswerInquiryRequest("답변");

        // when & then
        assertThatThrownBy(() -> adminService.answerInquiry(999L, inquiry.getId(), request))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 문의 ID로 답변 시도 시 예외 발생")
    void answerInquiry_Fail_InquiryNotFound() {
        // given
        AnswerInquiryRequest request = new AnswerInquiryRequest("답변");

        // when & then
        assertThatThrownBy(() -> adminService.answerInquiry(adminUser.getId(), 999L, request))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.INQUIRY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 신고 ID로 처리 시도 시 예외 발생")
    void processReport_Fail_ReportNotFound() {
        assertThatThrownBy(() -> adminService.processReport(999L))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.REPORT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 공지 ID로 삭제 시도 시 예외 발생")
    void deleteNotice_Fail_NoticeNotFound() {
        assertThatThrownBy(() -> adminService.deleteNotice(adminUser.getId(), 999L))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.NOTICE_NOT_FOUND.getMessage());
    }

    // --- Helper Methods ---

    private Club createAndSaveClub(Long id, String name, String region) {
        // Native Query로 ID를 강제 지정 (통계 초기화 로직 검증용)
        em.createNativeQuery("INSERT INTO clubs (club_id, name, region, home_stadium) VALUES (?, ?, ?, ?)")
                .setParameter(1, id)
                .setParameter(2, name)
                .setParameter(3, region)
                .setParameter(4, "Stadium")
                .executeUpdate();

        em.flush();
        em.clear();
        return clubRepository.findById(id).orElseThrow();
    }

    private User createUser(String email, String nickname, Club club, Authority authority, String watchStyle, char gender) {
        return User.builder()
                .email(email)
                .provider(Provider.GOOGLE)
                .providerId("google_" + email)
                .gender(gender)
                .nickName(nickname)
                .birthDate(LocalDate.of(1990, 1, 1))
                .club(club)
                .profileImageUrl("default.jpg")
                .allAlarm('Y').chatAlarm('Y').enrollAlarm('Y').eventAlarm('Y')
                .fcmToken("token_" + email)
                .authority(authority)
                .watchStyle(watchStyle)
                .isReported(false)
                .build();
    }

    private User createUser(String email, String nickname, Club club, Authority authority, String watchStyle) {
        return createUser(email, nickname, club, authority, watchStyle, 'M');
    }

    private Game createGame(Club club) {
        return gameRepository.save(Game.builder().homeClub(club).awayClub(club).gameStartDate(LocalDateTime.now()).location("Stadium").build());
    }

    private Board createAndSaveBoard(User user, Club club, boolean isCompleted) {
        Game game = gameRepository.findAll().get(0);
        Board board = Board.builder()
                .title("Title").content("Content").maxPerson(4).currentPerson(1)
                .user(user).club(club).game(game)
                .preferredGender("M").preferredAgeRange("20s").isCompleted(isCompleted)
                .liftUpDate(LocalDateTime.now())
                .build();

        return boardRepository.save(board);
    }

    private Report createAndSaveReport(User reporter, User reported) {
        return reportRepository.save(Report.builder()
                .reporter(reporter)
                .reportedUser(reported)
                .reportType(ReportType.PROFANITY)
                .content("욕설")
                .isProcessed(false)
                .build());
    }

    private Inquiry createAndSaveInquiry(User user) {
        return inquiryRepository.save(Inquiry.builder()
                .user(user)
                .inquiryType(InquiryType.OTHER)
                .content("내용")
                .isCompleted(false)
                .build());
    }

    private Notice createAndSaveNotice(User user, String title, String content) {
        return noticeRepository.save(Notice.builder()
                .user(user)
                .title(title)
                .content(content)
                .build());
    }
}
