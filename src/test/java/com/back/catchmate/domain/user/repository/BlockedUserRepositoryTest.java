package com.back.catchmate.domain.user.repository;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.BlockedUser;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BlockedUserRepositoryTest {

    @Autowired
    private BlockedUserRepository blockedUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    private User blockerUser; // 차단을 하는 사람 (주인공)
    private User targetUser1; // 차단 당할 사람 1
    private User targetUser2; // 차단 당할 사람 2
    private Club club;

    @BeforeEach
    void setUp() {
        // 1. 구단 데이터 생성 (User 생성 필수 조건)
        club = clubRepository.save(Club.builder()
                .name("KIA")
                .homeStadium("챔피언스필드")
                .region("광주")
                .build());

        // 2. 유저 데이터 생성
        blockerUser = saveUser("blocker", "token_1");
        targetUser1 = saveUser("hater1", "token_2");
        targetUser2 = saveUser("hater2", "token_3");
    }

    @Test
    @DisplayName("내가 차단한 유저들의 ID 목록을 조회한다 (삭제된 내역 제외)")
    void findBlockedUserIdListByUserId() {
        // given
        // target1은 활성 차단
        saveBlockedUser(blockerUser, targetUser1, false);
        // target2는 차단했다가 해제함 (Soft Delete)
        saveBlockedUser(blockerUser, targetUser2, true);

        // when
        // JPQL 쿼리 실행: SELECT b.blocked.id ...
        List<Long> blockedIds = blockedUserRepository.findBlockedUserIdListByUserId(blockerUser.getId());

        // then
        assertThat(blockedIds).hasSize(1);
        assertThat(blockedIds).contains(targetUser1.getId()); // target1만 있어야 함
        assertThat(blockedIds).doesNotContain(targetUser2.getId()); // 삭제된 target2는 없어야 함
    }

    @Test
    @DisplayName("특정 유저 차단 여부를 확인한다 (exists)")
    void existsByBlockerIdAndBlockedIdAndDeletedAtIsNull() {
        // given
        saveBlockedUser(blockerUser, targetUser1, false); // 차단 중

        // when
        boolean isBlocked = blockedUserRepository.existsByBlockerIdAndBlockedIdAndDeletedAtIsNull(
                blockerUser.getId(), targetUser1.getId());

        boolean isNotBlocked = blockedUserRepository.existsByBlockerIdAndBlockedIdAndDeletedAtIsNull(
                blockerUser.getId(), targetUser2.getId()); // 차단 안 함

        // then
        assertThat(isBlocked).isTrue();
        assertThat(isNotBlocked).isFalse();
    }

    @Test
    @DisplayName("차단 목록을 페이징하여 조회한다")
    void findAllByBlockerIdAndDeletedAtIsNull() {
        // given
        // 3명을 차단 (2명 활성, 1명 해제)
        User target3 = saveUser("hater3", "token_4");
        saveBlockedUser(blockerUser, targetUser1, false);
        saveBlockedUser(blockerUser, targetUser2, true); // 삭제됨
        saveBlockedUser(blockerUser, target3, false);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<BlockedUser> result = blockedUserRepository.findAllByBlockerIdAndDeletedAtIsNull(
                blockerUser.getId(), pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2); // target1, target3
        assertThat(result.getContent())
                .extracting(b -> b.getBlocked().getNickName())
                .containsExactlyInAnyOrder("hater1", "hater3");
    }

    @Test
    @DisplayName("차단 관계 엔티티를 단건 조회한다 (Optional)")
    void findByBlockerIdAndBlockedIdAndDeletedAtIsNull() {
        // given
        saveBlockedUser(blockerUser, targetUser1, false);

        // when
        Optional<BlockedUser> found = blockedUserRepository.findByBlockerIdAndBlockedIdAndDeletedAtIsNull(
                blockerUser.getId(), targetUser1.getId());

        Optional<BlockedUser> notFound = blockedUserRepository.findByBlockerIdAndBlockedIdAndDeletedAtIsNull(
                blockerUser.getId(), targetUser2.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getBlocked().getId()).isEqualTo(targetUser1.getId());

        assertThat(notFound).isEmpty();
    }

    // --- Helper Methods ---

    private User saveUser(String nickName, String fcmToken) {
        User user = User.builder()
                .email(nickName + "@test.com")
                .provider(Provider.GOOGLE)
                .providerId("pid_" + nickName)
                .gender('M')
                .nickName(nickName)
                .birthDate(LocalDate.of(2000, 1, 1))
                .club(club)
                .watchStyle("직관")
                .profileImageUrl("img.url")
                .allAlarm('Y')
                .chatAlarm('Y')
                .enrollAlarm('Y')
                .eventAlarm('Y')
                .fcmToken(fcmToken)
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build();
        return userRepository.save(user);
    }

    private void saveBlockedUser(User blocker, User blocked, boolean isDeleted) {
        BlockedUser blockedUser = BlockedUser.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        if (isDeleted) {
            blockedUser.delete(); // BaseTimeEntity의 delete() 호출 가정
        }

        blockedUserRepository.save(blockedUser);
    }
}
