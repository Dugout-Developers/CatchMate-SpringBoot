package com.back.catchmate.domain.enroll.repository;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test") // application-test.properties 설정 사용
@SpringBootTest // 애플리케이션의 모든 빈을 로드하는 통합 테스트
@Transactional  // 각 테스트 메서드 실행 후 롤백하여 데이터 정합성 유지
class EnrollRepositoryTest {
    @Autowired
    private EnrollRepository enrollRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private GameRepository gameRepository;

    private User writer;
    private User applicant;
    private Board board;
    private Club club;

    @BeforeEach
    void setUp() {
        // 연관 관계에 필요한 기초 데이터 셋업
        club = clubRepository.save(Club.builder()
                .name("Test Club")
                .region("Seoul")
                .homeStadium("Stadium")
                .build());

        Game game = gameRepository.save(Game.builder()
                .homeClub(club).awayClub(club)
                .gameStartDate(LocalDateTime.now().plusDays(1))
                .location("Stadium")
                .build());

        writer = userRepository.save(createUser("writer", club));
        applicant = userRepository.save(createUser("applicant", club));

        board = boardRepository.save(Board.builder()
                .title("Test Board")
                .content("Content")
                .maxPerson(4)
                .currentPerson(1)
                .user(writer)
                .club(club)
                .game(game)
                .preferredGender("ANY")
                .preferredAgeRange("20")
                .isCompleted(true)
                .liftUpDate(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("게시글 작성자가 받은 신청 목록 조회 - 조인 및 필터링 검증")
    void findEnrollListByBoardWriter() {
        // given
        // 1. 정상 신청 (PENDING) -> 조회 대상
        Enroll pendingEnroll = enrollRepository.save(createEnroll(applicant, board, AcceptStatus.PENDING, true));

        // 2. 이미 수락된 신청 (ACCEPTED) -> 조회 제외
        enrollRepository.save(createEnroll(applicant, board, AcceptStatus.ACCEPTED, true));

        // 3. 삭제된 신청 (Soft Delete) -> 조회 제외
        Enroll deletedEnroll = createEnroll(applicant, board, AcceptStatus.PENDING, true);
        deletedEnroll.delete();
        enrollRepository.save(deletedEnroll);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Enroll> result = enrollRepository.findEnrollListByBoardWriter(writer.getId(), pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(pendingEnroll.getId());

        // Fetch Join 등을 통해 Board 정보도 함께 로딩되었는지 확인 (지연 로딩 에러가 안 나는지)
        assertThat(result.getContent().get(0).getBoard().getTitle()).isEqualTo("Test Board");
    }

    @Test
    @DisplayName("특정 게시글의 PENDING 상태 신청 목록 조회")
    void findByBoardIdAndDeletedAtIsNull() {
        // given
        Enroll enroll1 = enrollRepository.save(createEnroll(applicant, board, AcceptStatus.PENDING, true));
        // 거절된 신청은 조회되지 않아야 함
        enrollRepository.save(createEnroll(applicant, board, AcceptStatus.REJECTED, true));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Enroll> result = enrollRepository.findByBoardIdAndDeletedAtIsNull(board.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(enroll1.getId());
    }

    @Test
    @DisplayName("작성자의 새로운(isNew=true) 신청 개수 카운트")
    void countNewEnrollListByUserId() {
        // given
        // 1. 새로운 신청
        enrollRepository.save(createEnroll(applicant, board, AcceptStatus.PENDING, true));
        // 2. 읽은 신청 (isNew = false)
        enrollRepository.save(createEnroll(applicant, board, AcceptStatus.PENDING, false));

        // 3. 같은 작성자의 다른 게시글에 대한 새로운 신청
        Board anotherBoard = boardRepository.save(Board.builder()
                .title("Another Board").content("Content").maxPerson(4).currentPerson(1)
                .user(writer).club(club).game(board.getGame())
                .preferredGender("ANY").preferredAgeRange("20").isCompleted(true).liftUpDate(LocalDateTime.now())
                .build());
        enrollRepository.save(createEnroll(applicant, anotherBoard, AcceptStatus.PENDING, true));

        // when
        int count = enrollRepository.countNewEnrollListByUserId(writer.getId());

        // then
        assertThat(count).isEqualTo(2); // 1번 + 3번
    }

    @Test
    @DisplayName("ID로 신청 조회 (Soft Delete된 데이터 제외 확인)")
    void findByIdAndDeletedAtIsNull() {
        // given
        Enroll enroll = enrollRepository.save(createEnroll(applicant, board, AcceptStatus.PENDING, true));

        Enroll deletedEnroll = createEnroll(applicant, board, AcceptStatus.PENDING, true);
        deletedEnroll.delete();
        enrollRepository.save(deletedEnroll);

        // when
        Optional<Enroll> found = enrollRepository.findByIdAndDeletedAtIsNull(enroll.getId());
        Optional<Enroll> notFound = enrollRepository.findByIdAndDeletedAtIsNull(deletedEnroll.getId());

        // then
        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("유저 ID와 게시글 ID로 신청 내역 단건 조회")
    void findByUserIdAndBoardIdAndDeletedAtIsNull() {
        // given
        Enroll enroll = enrollRepository.save(createEnroll(applicant, board, AcceptStatus.PENDING, true));

        // when
        Optional<Enroll> result = enrollRepository.findByUserIdAndBoardIdAndDeletedAtIsNull(applicant.getId(), board.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(enroll.getId());
    }

    @Test
    @DisplayName("비관적 락(Pessimistic Lock)을 이용한 조회 쿼리 실행 확인")
    void findByIdWithLock() {
        // given
        Enroll enroll = enrollRepository.save(createEnroll(applicant, board, AcceptStatus.PENDING, true));

        // when
        Optional<Enroll> result = enrollRepository.findByIdWithLock(enroll.getId());

        // then
        assertThat(result).isPresent();
        // 실제 락 동작은 동시성 테스트가 필요하지만, SQL 문법 오류 없이 쿼리가 실행되는지 확인
    }

    // --- Helper Methods ---

    private User createUser(String nickname, Club club) {
        return User.builder()
                .email(nickname + "@test.com")
                .provider(Provider.GOOGLE)
                .providerId("id_" + nickname)
                .gender('M')
                .nickName(nickname)
                .birthDate(LocalDate.now())
                .club(club)
                .profileImageUrl("url")
                .allAlarm('Y').chatAlarm('Y').enrollAlarm('Y').eventAlarm('Y')
                .fcmToken("token")
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build();
    }

    private Enroll createEnroll(User user, Board board, AcceptStatus status, boolean isNew) {
        return Enroll.builder()
                .user(user)
                .board(board)
                .acceptStatus(status)
                .description("신청합니다")
                .isNew(isNew)
                .build();
    }
}
