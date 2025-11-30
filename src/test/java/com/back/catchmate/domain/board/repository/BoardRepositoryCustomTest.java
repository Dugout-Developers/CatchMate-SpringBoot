package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.BlockedUser;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.BlockedUserRepository;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.config.JpaConfig;
import com.back.catchmate.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class})
@ActiveProfiles("test")
class BoardRepositoryTest {

    @Autowired private BoardRepository boardRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private BlockedUserRepository blockedUserRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private EntityManager em; // 필드 추가

    private User userA, userB, userBlocked;
    private Club kia, doosan;
    private Game gameToday, gameTomorrow;

    @BeforeEach
    void setUp() {
        // 1. 구단 생성
        kia = clubRepository.save(createClub("KIA Tigers", "Gwangju"));
        doosan = clubRepository.save(createClub("Doosan Bears", "Seoul"));

        // 2. 유저 생성
        userA = userRepository.save(createUser("userA@test.com", "UserA", kia));
        userB = userRepository.save(createUser("userB@test.com", "UserB", doosan));
        userBlocked = userRepository.save(createUser("blocked@test.com", "BlockedUser", kia));

        // 3. 게임 생성
        gameToday = gameRepository.save(createGame(kia, doosan, LocalDateTime.now()));
        gameTomorrow = gameRepository.save(createGame(doosan, kia, LocalDateTime.now().plusDays(1)));
    }

    @Test
    @DisplayName("기본 필터링 조회 - 날짜, 완료 여부, 삭제 여부 확인")
    void findFilteredBoards_Basic() {
        // given
        // 1. 정상 게시글 (오늘 경기)
        createAndSaveBoard("정상글", userA, kia, gameToday, true);
        // 2. 날짜가 다른 게시글 (내일 경기)
        createAndSaveBoard("내일글", userB, doosan, gameTomorrow, true);
        // 3. 미완료 게시글 (임시저장)
        createAndSaveBoard("임시글", userA, kia, gameToday, false);
        // 4. 삭제된 게시글
        Board deletedBoard = createAndSaveBoard("삭제글", userB, doosan, gameToday, true);
        deletedBoard.deleteBoard();
        boardRepository.save(deletedBoard);

        // when
        // 오늘 날짜로 조회
        Page<Board> result = boardRepository.findFilteredBoards(
                userA.getId(),
                LocalDate.now(),
                null,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("정상글");
    }

    @Test
    @DisplayName("상세 필터링 조회 - 인원수 및 선호 구단")
    void findFilteredBoards_ComplexFilter() {
        // given
        // 조건에 맞는 글: KIA 응원, 4명 모집
        createAndSaveBoard("KIA 4명", userA, kia, gameToday, true, 4);
        // 조건 불일치: DOOSAN 응원
        createAndSaveBoard("Doosan 4명", userB, doosan, gameToday, true, 4);
        // 조건 불일치: 2명 모집
        createAndSaveBoard("KIA 2명", userA, kia, gameToday, true, 2);

        // when
        // KIA(preferredTeamIdList) 이면서 4명(maxPerson) 모집하는 글 조회
        Page<Board> result = boardRepository.findFilteredBoards(
                userA.getId(),
                LocalDate.now(),
                4,
                List.of(kia.getId()),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("KIA 4명");
    }

    @Test
    @DisplayName("차단 유저 필터링 - 내가 차단한 유저의 글은 보이지 않아야 한다")
    void findFilteredBoards_BlockedUser() {
        // given
        // userA가 userBlocked를 차단
        blockedUserRepository.save(BlockedUser.builder().blocker(userA).blocked(userBlocked).build());

        // userBlocked가 쓴 글
        createAndSaveBoard("차단된 유저의 글", userBlocked, kia, gameToday, true);
        // userB가 쓴 글 (정상)
        createAndSaveBoard("일반 유저의 글", userB, doosan, gameToday, true);

        // when
        // userA가 조회
        Page<Board> result = boardRepository.findFilteredBoards(
                userA.getId(),
                null, // 날짜 필터 없음
                null,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("일반 유저의 글");
    }

    @Test
    @DisplayName("비회원(Guest) 조회 - 차단 필터링 없이 모든 글이 보여야 한다")
    void findFilteredBoards_Guest() {
        // given
        // userBlocked가 쓴 글 (하지만 Guest는 차단 관계가 없으므로 보여야 함)
        createAndSaveBoard("차단된 유저의 글", userBlocked, kia, gameToday, true);
        createAndSaveBoard("일반 유저의 글", userB, doosan, gameToday, true);

        // when
        // userId = null (비회원)
        Page<Board> result = boardRepository.findFilteredBoards(
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // --- Helper Methods ---

    private Club createClub(String name, String region) {
        return Club.builder().name(name).region(region).homeStadium("Stadium").build();
    }

    private User createUser(String email, String nickname, Club club) {
        return User.builder()
                .email(email)
                .provider(Provider.GOOGLE)
                .providerId("google_" + email)
                .gender('M')
                .nickName(nickname)
                .birthDate(LocalDate.of(1990, 1, 1))
                .club(club)
                .profileImageUrl("img.jpg")
                .allAlarm('Y').chatAlarm('Y').enrollAlarm('Y').eventAlarm('Y')
                .fcmToken("token")
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build();
    }

    private Game createGame(Club home, Club away, LocalDateTime date) {
        return Game.builder().homeClub(home).awayClub(away).gameStartDate(date).location("Gwangju").build();
    }

    private Board createAndSaveBoard(String title, User user, Club club, Game game, boolean isCompleted) {
        return createAndSaveBoard(title, user, club, game, isCompleted, 4);
    }

    private Board createAndSaveBoard(String title, User user, Club club, Game game, boolean isCompleted, int maxPerson) {
        Board board = Board.builder()
                .title(title)
                .content("내용")
                .maxPerson(maxPerson)
                .currentPerson(1)
                .user(user)
                .club(club)
                .game(game)
                .preferredGender("M")
                .preferredAgeRange("20s")
                .isCompleted(isCompleted)
                .liftUpDate(LocalDateTime.now())
                .build();

        board = boardRepository.save(board);

        if (isCompleted) {
            ChatRoom chatRoom = ChatRoom.builder().board(board).participantCount(1).build();
            chatRoomRepository.save(chatRoom);

            // [중요] 테스트 코드 내 객체 정합성을 위해 영속성 컨텍스트 초기화
            // 이렇게 하면 반환되는 board는 Detached 상태일 수 있으므로, 호출 측에서 주의하거나
            // 단순히 em.refresh(board)를 호출하여 연관관계를 로딩합니다.
            em.flush();
            em.refresh(board);
        }
        return board;
    }
}
