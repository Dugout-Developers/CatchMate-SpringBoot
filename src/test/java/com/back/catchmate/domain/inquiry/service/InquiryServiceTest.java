package com.back.catchmate.domain.inquiry.service;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.inquiry.dto.InquiryRequest.CreateInquiryRequest;
import com.back.catchmate.domain.inquiry.dto.InquiryResponse.InquiryInfo;
import com.back.catchmate.domain.inquiry.entity.Inquiry;
import com.back.catchmate.domain.inquiry.entity.InquiryType;
import com.back.catchmate.domain.inquiry.repository.InquiryRepository;
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

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class InquiryServiceTest {

    @Autowired private InquiryService inquiryService;
    @Autowired private InquiryRepository inquiryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;

    private User user;
    private Club club;

    @BeforeEach
    void setUp() {
        club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        user = userRepository.save(createUser("user@test.com", "user", club));
    }

    @Test
    @DisplayName("문의 등록 성공 - DB에 문의 데이터가 저장되어야 한다")
    void submitInquiry_Success() {
        // given
        CreateInquiryRequest request = new CreateInquiryRequest(InquiryType.OTHER, "문의 내용입니다.");

        // when
        StateResponse response = inquiryService.submitInquiry(user.getId(), request);

        // then
        assertThat(response.isState()).isTrue();

        // DB 검증
        Inquiry savedInquiry = inquiryRepository.findAll().stream()
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("문의가 저장되지 않았습니다."));

        assertThat(savedInquiry.getContent()).isEqualTo("문의 내용입니다.");
        assertThat(savedInquiry.getInquiryType()).isEqualTo(InquiryType.OTHER);
        assertThat(savedInquiry.getIsCompleted()).isFalse(); // 기본값 확인
    }

    @Test
    @DisplayName("존재하지 않는 유저가 문의 등록 시 예외가 발생한다")
    void submitInquiry_Fail_UserNotFound() {
        // given
        CreateInquiryRequest request = new CreateInquiryRequest(InquiryType.OTHER, "문의 내용");
        Long invalidUserId = 99999L;

        // when & then
        assertThatThrownBy(() -> inquiryService.submitInquiry(invalidUserId, request))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("문의 상세 조회 성공")
    void getInquiry_Success() {
        // given
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .user(user)
                .inquiryType(InquiryType.ACCOUNT)
                .content("계정 관련 문의입니다.")
                .isCompleted(false)
                .build());

        // when
        InquiryInfo info = inquiryService.getInquiry(inquiry.getId());

        // then
        assertThat(info.getInquiryId()).isEqualTo(inquiry.getId());
        assertThat(info.getContent()).isEqualTo("계정 관련 문의입니다.");
        assertThat(info.getInquiryType()).isEqualTo(InquiryType.ACCOUNT);
        assertThat(info.getNickName()).isEqualTo(user.getNickName());
    }

    @Test
    @DisplayName("존재하지 않는 문의 조회 시 예외가 발생한다")
    void getInquiry_Fail_NotFound() {
        // given
        Long invalidInquiryId = 99999L;

        // when & then
        assertThatThrownBy(() -> inquiryService.getInquiry(invalidInquiryId))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("존재하지 않는 문의입니다.");
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
