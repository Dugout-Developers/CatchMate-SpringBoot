package com.back.catchmate.domain.notice.service;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.notice.dto.NoticeResponse.NoticeInfo;
import com.back.catchmate.domain.notice.dto.NoticeResponse.PagedNoticeInfo;
import com.back.catchmate.domain.notice.entity.Notice;
import com.back.catchmate.domain.notice.repository.NoticeRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class NoticeServiceTest {

    @Autowired private NoticeService noticeService;
    @Autowired private NoticeRepository noticeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;

    private User adminUser;

    @BeforeEach
    void setUp() {
        Club club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        adminUser = userRepository.save(createUser("admin@test.com", "admin", club));
    }

    @Test
    @DisplayName("공지사항 목록 조회 성공")
    void getNoticeList_Success() {
        // given
        createAndSaveNotice("공지1", "내용1");
        createAndSaveNotice("공지2", "내용2");
        createAndSaveNotice("공지3", "내용3");

        // when
        PagedNoticeInfo result = noticeService.getNoticeList(PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNoticeInfoList())
                .extracting("title")
                .containsExactlyInAnyOrder("공지1", "공지2", "공지3");
    }

    @Test
    @DisplayName("공지사항 단건 조회 성공")
    void getNotice_Success() {
        // given
        Notice notice = createAndSaveNotice("긴급 점검", "서버 점검 안내");

        // when
        NoticeInfo info = noticeService.getNotice(notice.getId());

        // then
        assertThat(info.getNoticeId()).isEqualTo(notice.getId());
        assertThat(info.getTitle()).isEqualTo("긴급 점검");
        assertThat(info.getContent()).isEqualTo("서버 점검 안내");
        assertThat(info.getUserInfo().getNickName()).isEqualTo(adminUser.getNickName());
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 조회 시 예외가 발생한다")
    void getNotice_Fail_NotFound() {
        // given
        Long invalidId = 99999L;

        // when & then
        assertThatThrownBy(() -> noticeService.getNotice(invalidId))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("존재하지 않는 공지입니다.");
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
                .authority(Authority.ROLE_ADMIN) // 공지사항 작성자이므로 ADMIN 권한 부여 (로직상 필수는 아님)
                .isReported(false)
                .build();
    }

    private Notice createAndSaveNotice(String title, String content) {
        Notice notice = Notice.builder()
                .user(adminUser)
                .title(title)
                .content(content)
                .build();
        return noticeRepository.save(notice);
    }
}
