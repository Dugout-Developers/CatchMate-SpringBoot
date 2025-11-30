package com.back.catchmate.domain.user.repository;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import org.junit.jupiter.api.AfterEach;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    private Club kiaTigers;
    private Club samsungLions;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        // [Given] 공통 데이터 준비: 구단 생성
        kiaTigers = Club.builder().name("KIA").homeStadium("챔필").region("광주").build();
        samsungLions = Club.builder().name("Samsung").homeStadium("라팍").region("대구").build();
        clubRepository.saveAll(List.of(kiaTigers, samsungLions));
    }

//    @AfterEach
//    void tearDown() {
//        // 테스트 수행 후 생성된 데이터를 모두 삭제하여 DB 상태를 깨끗하게 비웁니다.
//        userRepository.deleteAll();
//        clubRepository.deleteAll();
//    }

    @Test
    @DisplayName("닉네임 중복 여부를 확인한다 (existsByNickName)")
    void existsByNickName() {
        // given
        saveUser("유저1", kiaTigers, false);

        // when & then
        assertThat(userRepository.existsByNickName("유저1")).isTrue();
        assertThat(userRepository.existsByNickName("없는닉네임")).isFalse();
    }

    @Test
    @DisplayName("성별로 활성 유저(삭제되지 않은) 수를 카운트한다")
    void countByGenderAndDeletedAtIsNull() {
        // given
        saveUser("남1", kiaTigers, 'M', false); // 남자, 활성
        saveUser("남2", kiaTigers, 'M', true);  // 남자, 탈퇴 (카운트 제외되어야 함)
        saveUser("여1", samsungLions, 'F', false); // 여자, 활성

        // when
        long maleCount = userRepository.countByGenderAndDeletedAtIsNull('M');
        long femaleCount = userRepository.countByGenderAndDeletedAtIsNull('F');

        // then
        assertThat(maleCount).isEqualTo(1); // 탈퇴한 남2 제외
        assertThat(femaleCount).isEqualTo(1);
    }

    @Test
    @DisplayName("ID로 유저 조회 시 삭제된 유저는 조회되지 않아야 한다")
    void findByIdAndDeletedAtIsNull() {
        // given
        User activeUser = saveUser("활성유저", kiaTigers, false);
        User deletedUser = saveUser("탈퇴유저", kiaTigers, true);

        // when
        Optional<User> foundActive = userRepository.findByIdAndDeletedAtIsNull(activeUser.getId());
        Optional<User> foundDeleted = userRepository.findByIdAndDeletedAtIsNull(deletedUser.getId());

        // then
        assertThat(foundActive).isPresent();
        assertThat(foundActive.get().getNickName()).isEqualTo("활성유저");

        assertThat(foundDeleted).isEmpty(); // 삭제된 유저는 Optional.empty()
    }

    @Test
    @DisplayName("모든 활성 유저를 페이징하여 조회한다")
    void findAllByDeletedAtIsNull() {
        // given
        for (int i = 1; i <= 5; i++) {
            saveUser("유저" + i, kiaTigers, false);
        }
        saveUser("탈퇴유저", kiaTigers, true); // 조회되면 안 됨

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.findAllByDeletedAtIsNull(pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(5); // 탈퇴 유저 제외
        assertThat(result.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("Provider ID로 존재 여부를 확인한다 (삭제된 유저 제외)")
    void existsByProviderIdAndDeletedAtIsNull() {
        // given
        User activeUser = saveUser("활성", kiaTigers, false);
        User deletedUser = saveUser("탈퇴", kiaTigers, true);

        // when & then
        assertThat(userRepository.existsByProviderIdAndDeletedAtIsNull(activeUser.getProviderId())).isTrue();
        assertThat(userRepository.existsByProviderIdAndDeletedAtIsNull(deletedUser.getProviderId())).isFalse();
    }

    @Test
    @DisplayName("Provider ID로 유저를 조회한다 (삭제된 유저 제외)")
    void findByProviderIdAndDeletedAtIsNull() {
        // given
        User activeUser = saveUser("활성", kiaTigers, false);
        User deletedUser = saveUser("탈퇴", kiaTigers, true);

        // when
        Optional<User> found = userRepository.findByProviderIdAndDeletedAtIsNull(activeUser.getProviderId());
        Optional<User> notFound = userRepository.findByProviderIdAndDeletedAtIsNull(deletedUser.getProviderId());

        // then
        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("구단 이름으로 유저를 페이징 조회한다 (Join 쿼리 검증)")
    void findByClubNameAndDeletedAtIsNull() {
        // given
        saveUser("KIA팬1", kiaTigers, false);
        saveUser("KIA팬2", kiaTigers, false);
        saveUser("KIA탈퇴", kiaTigers, true); // 제외
        saveUser("삼성팬1", samsungLions, false); // 제외

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<User> result = userRepository.findByClubNameAndDeletedAtIsNull("KIA", pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2); // KIA팬1, KIA팬2
        assertThat(result.getContent())
                .extracting("nickName")
                .containsExactlyInAnyOrder("KIA팬1", "KIA팬2");
    }

    // --- Helper Methods ---

    // 기본 생성 (성별 남성 고정)
    private User saveUser(String nickName, Club club, boolean isDeleted) {
        return saveUser(nickName, club, 'M', isDeleted);
    }

    // 성별 지정 생성
    private User saveUser(String nickName, Club club, char gender, boolean isDeleted) {
        User user = User.builder()
                .email(nickName + "@test.com")
                .provider(Provider.GOOGLE)
                .providerId("pid_" + nickName)
                .gender(gender)
                .nickName(nickName)
                .birthDate(LocalDate.of(2000, 1, 1))
                .club(club)
                .watchStyle("직관")
                .profileImageUrl("http://img.com")
                .allAlarm('Y')
                .chatAlarm('Y')
                .enrollAlarm('Y')
                .eventAlarm('Y')
                .fcmToken("token_" + nickName) // 필수값 처리 (이전 에러 해결!)
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build();

        if (isDeleted) {
            user.deleteUser();
        }

        return userRepository.save(user);
    }
}
