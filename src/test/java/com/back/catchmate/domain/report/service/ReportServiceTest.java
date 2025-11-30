package com.back.catchmate.domain.report.service;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.report.dto.ReportRequest.CreateReportRequest;
import com.back.catchmate.domain.report.entity.Report;
import com.back.catchmate.domain.report.entity.ReportType;
import com.back.catchmate.domain.report.repository.ReportRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class ReportServiceTest {

    @Autowired private ReportService reportService;
    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;

    private User reporter;
    private User reportedUser;
    private Club club;

    @BeforeEach
    void setUp() {
        club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        reporter = userRepository.save(createUser("reporter@test.com", "reporter", club));
        reportedUser = userRepository.save(createUser("reported@test.com", "reported", club));
    }

    @Test
    @DisplayName("유저 신고 성공 - DB에 신고 데이터가 저장되어야 한다")
    void reportUser_Success() {
        // given
        CreateReportRequest request = CreateReportRequest.builder()
                .reportType(ReportType.PROFANITY)
                .content("욕설을 했습니다.")
                .build();

        // when
        StateResponse response = reportService.reportUser(reporter.getId(), reportedUser.getId(), request);

        // then
        assertThat(response.isState()).isTrue();

        // DB 검증
        Report savedReport = reportRepository.findAll().stream()
                .filter(r -> r.getReporter().getId().equals(reporter.getId()) &&
                        r.getReportedUser().getId().equals(reportedUser.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("신고 데이터가 저장되지 않았습니다."));

        assertThat(savedReport.getReportType()).isEqualTo(ReportType.PROFANITY);
        assertThat(savedReport.getContent()).isEqualTo("욕설을 했습니다.");
        assertThat(savedReport.getIsProcessed()).isFalse(); // 기본값 false 확인
    }

    @Test
    @DisplayName("존재하지 않는 신고자가 신고할 경우 예외가 발생한다")
    void reportUser_Fail_ReporterNotFound() {
        // given
        Long invalidReporterId = 99999L;
        CreateReportRequest request = CreateReportRequest.builder()
                .reportType(ReportType.SPAM)
                .content("도배")
                .build();

        // when & then
        assertThatThrownBy(() -> reportService.reportUser(invalidReporterId, reportedUser.getId(), request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 대상을 신고할 경우 예외가 발생한다")
    void reportUser_Fail_ReportedUserNotFound() {
        // given
        Long invalidReportedUserId = 99999L;
        CreateReportRequest request = CreateReportRequest.builder()
                .reportType(ReportType.ADVERTISEMENT)
                .content("광고")
                .build();

        // when & then
        assertThatThrownBy(() -> reportService.reportUser(reporter.getId(), invalidReportedUserId, request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    // --- Helper Methods ---
    private User createUser(String email, String nickname, Club club) {
        return User.builder()
                .email(email)
                .provider(Provider.GOOGLE)
                .providerId("google_" + email)
                .gender('M')
                .nickName(nickname)
                .birthDate(LocalDate.of(1990, 1, 1))
                .club(club)
                .profileImageUrl("default.jpg")
                .allAlarm('Y').chatAlarm('Y').enrollAlarm('Y').eventAlarm('Y')
                .fcmToken("token_" + email)
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build();
    }
}
