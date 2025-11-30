package com.back.catchmate.domain.user.service;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.user.dto.UserResponse.PagedUserInfo;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.BlockedUserRepository;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
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

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BlockedUserServiceTest {

    @Autowired private BlockedUserService blockedUserService;
    @Autowired private BlockedUserRepository blockedUserRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;

    private User blocker;
    private User blocked;
    private Club club;

    @BeforeEach
    void setUp() {
        // 데이터 격리를 위한 초기화
        blockedUserRepository.deleteAll();
        userRepository.deleteAll();
        clubRepository.deleteAll();

        club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        blocker = userRepository.save(createUser("blocker@test.com", "blocker", club));
        blocked = userRepository.save(createUser("blocked@test.com", "blocked", club));
    }

    @Test
    @DisplayName("유저 차단 성공 - DB에 차단 데이터가 저장되어야 한다")
    void blockUser_Success() {
        // when
        StateResponse response = blockedUserService.blockUser(blocker.getId(), blocked.getId());

        // then
        assertThat(response.isState()).isTrue();

        // DB 검증
        boolean exists = blockedUserRepository.existsByBlockerIdAndBlockedIdAndDeletedAtIsNull(blocker.getId(), blocked.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("자기 자신을 차단하면 예외가 발생한다")
    void blockUser_Fail_SelfBlock() {
        // when & then
        assertThatThrownBy(() -> blockedUserService.blockUser(blocker.getId(), blocker.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("자기 자신을 차단할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 차단한 유저를 다시 차단하면 예외가 발생한다")
    void blockUser_Fail_AlreadyBlocked() {
        // given
        blockedUserService.blockUser(blocker.getId(), blocked.getId());

        // when & then
        assertThatThrownBy(() -> blockedUserService.blockUser(blocker.getId(), blocked.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("해당 유저를 이미 차단했습니다.");
    }

    @Test
    @DisplayName("차단 해제 성공 - 데이터가 Soft Delete 되어야 한다")
    void unblockUser_Success() {
        // given
        blockedUserService.blockUser(blocker.getId(), blocked.getId());

        // when
        StateResponse response = blockedUserService.unblockUser(blocker.getId(), blocked.getId());

        // then
        assertThat(response.isState()).isTrue();

        // DB 검증 (조회되지 않아야 함)
        boolean exists = blockedUserRepository.existsByBlockerIdAndBlockedIdAndDeletedAtIsNull(blocker.getId(), blocked.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("차단하지 않은 유저를 해제하려 하면 예외가 발생한다")
    void unblockUser_Fail_NotBlocked() {
        // when & then
        assertThatThrownBy(() -> blockedUserService.unblockUser(blocker.getId(), blocked.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("해당 유저를 차단한 이력이 없습니다.");
    }

    @Test
    @DisplayName("차단 목록 조회 성공")
    void getBlockedUserList_Success() {
        // given
        blockedUserService.blockUser(blocker.getId(), blocked.getId());

        // when
        PagedUserInfo result = blockedUserService.getBlockedUserList(blocker.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getUserInfoList()).hasSize(1);
        assertThat(result.getUserInfoList().get(0).getUserId()).isEqualTo(blocked.getId());
        assertThat(result.getUserInfoList().get(0).getNickName()).isEqualTo("blocked");
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
