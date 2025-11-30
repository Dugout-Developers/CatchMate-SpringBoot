package com.back.catchmate.domain.club.service;

import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfoList;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.global.error.exception.BaseException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ClubServiceTest {

    @Autowired private ClubService clubService;
    @Autowired private ClubRepository clubRepository;
    @Autowired private EntityManager em;

    private Club kiaClub;

    @BeforeEach
    void setUp() {
        // 데이터 격리를 위해 초기화
        clubRepository.deleteAll();

        // [중요] 기본 구단(ID=0) 생성
        // JPA의 @GeneratedValue는 0번 ID 생성을 막는 경우가 많아 Native Query로 강제 삽입
        em.createNativeQuery("INSERT INTO clubs (club_id, name, region, home_stadium) VALUES (0, 'Default Club', 'None', 'None')")
                .executeUpdate();

        // 영속성 컨텍스트에 반영된 내용을 조회하여 객체로 보관
        Club defaultClub = clubRepository.findById(0L).orElseThrow();

        // 일반 구단 생성
        kiaClub = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .region("Gwangju")
                .homeStadium("Champions Field")
                .build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("구단 목록 조회 성공")
    void getClubInfoList_Success() {
        // when
        ClubInfoList result = clubService.getClubInfoList();

        // then
        // 0번 구단 + KIA 구단 = 총 2개
        assertThat(result.getClubInfoList()).hasSize(2);
        assertThat(result.getClubInfoList())
                .extracting("name")
                .contains("Default Club", "KIA Tigers");
    }

    @Test
    @DisplayName("구단 단건 조회 성공 - 유효한 ID")
    void getClub_Success_ValidId() {
        // when
        Club result = clubService.getClub(kiaClub.getId());

        // then
        assertThat(result.getId()).isEqualTo(kiaClub.getId());
        assertThat(result.getName()).isEqualTo("KIA Tigers");
    }

    @Test
    @DisplayName("구단 단건 조회 - ID가 null이면 기본 구단(ID=0)을 반환한다")
    void getClub_Success_NullId() {
        // when
        Club result = clubService.getClub(null);

        // then
        assertThat(result.getId()).isEqualTo(0L);
        assertThat(result.getName()).isEqualTo("Default Club");
    }

    @Test
    @DisplayName("구단 단건 조회 - ID가 0이면 기본 구단(ID=0)을 반환한다")
    void getClub_Success_ZeroId() {
        // when
        Club result = clubService.getClub(0L);

        // then
        assertThat(result.getId()).isEqualTo(0L);
        assertThat(result.getName()).isEqualTo("Default Club");
    }

    @Test
    @DisplayName("존재하지 않는 구단 ID로 조회 시 예외가 발생한다")
    void getClub_Fail_NotFound() {
        // given
        Long invalidId = 99999L;

        // when & then
        assertThatThrownBy(() -> clubService.getClub(invalidId))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("존재하지 않는 구단입니다.");
    }
}
