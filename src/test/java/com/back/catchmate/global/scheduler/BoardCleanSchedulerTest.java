package com.back.catchmate.global.scheduler;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.config.JpaConfig;
import com.back.catchmate.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import({QueryDslConfig.class, JpaConfig.class})
class BoardCleanSchedulerTest {

    @Autowired private BoardCleanScheduler boardCleanScheduler;
    @Autowired private BoardRepository boardRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private EntityManager em; // [필수] 영속성 컨텍스트 제어용

    @MockBean private ChatMessageRepository chatMessageRepository;

    private User user;
    private Club club;

    @BeforeEach
    void setUp() {
        // 데이터 격리
        chatRoomRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
        clubRepository.deleteAll();

        club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        user = userRepository.save(createUser("user@test.com", "User", club));
    }

    @Test
    @DisplayName("오래된 게시글 삭제 - 경기일로부터 7일이 지난 게시글과 채팅 데이터는 삭제되어야 한다")
    void softDeleteOldBoardsAndChats_Success() {
        // given
        // 1. 삭제 대상 게시글 (8일 전 경기)
        LocalDateTime oldDate = LocalDateTime.now().minusDays(8);
        Game oldGame = gameRepository.save(createGame(club, oldDate));
        Board oldBoard = createAndSaveBoard("오래된 글", user, club, oldGame);
        // 이제 createAndSaveBoard에서 refresh를 하므로 getChatRoom()이 null이 아님
        Long oldChatRoomId = oldBoard.getChatRoom().getId();

        // 2. 유지 대상 게시글 (내일 경기)
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        Game futureGame = gameRepository.save(createGame(club, futureDate));
        Board recentBoard = createAndSaveBoard("최신 글", user, club, futureGame);
        Long recentChatRoomId = recentBoard.getChatRoom().getId();

        // when
        boardCleanScheduler.softDeleteOldBoardsAndChats();

        // then
        // 1. 오래된 게시글 삭제 확인
        Board deletedBoard = boardRepository.findById(oldBoard.getId()).orElseThrow();
        assertThat(deletedBoard.getDeletedAt()).isNotNull();

        // 2. 최신 게시글 유지 확인
        Board aliveBoard = boardRepository.findById(recentBoard.getId()).orElseThrow();
        assertThat(aliveBoard.getDeletedAt()).isNull();

        // 3. 채팅 메시지 삭제 로직 호출 검증
        verify(chatMessageRepository, times(1)).deleteAllByChatRoomId(oldChatRoomId);
        verify(chatMessageRepository, times(0)).deleteAllByChatRoomId(recentChatRoomId);
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

    private Game createGame(Club club, LocalDateTime date) {
        return Game.builder()
                .homeClub(club)
                .awayClub(club)
                .gameStartDate(date)
                .location("Gwangju")
                .build();
    }

    private Board createAndSaveBoard(String title, User user, Club club, Game game) {
        Board board = Board.builder()
                .title(title)
                .content("내용")
                .maxPerson(4)
                .currentPerson(1)
                .user(user)
                .club(club)
                .game(game)
                .preferredGender("M")
                .preferredAgeRange("20s") // 필수값
                .isCompleted(true)
                .liftUpDate(LocalDateTime.now())
                .build();

        board = boardRepository.save(board);

        ChatRoom chatRoom = ChatRoom.builder()
                .board(board)
                .participantCount(1)
                .userChatRoomList(new ArrayList<>())
                .build();
        chatRoomRepository.save(chatRoom);

        // [핵심 수정] DB에 저장된 ChatRoom 관계를 Board 객체에 반영
        em.flush();
        em.refresh(board);

        return board;
    }
}
