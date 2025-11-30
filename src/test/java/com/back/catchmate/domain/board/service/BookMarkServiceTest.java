package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.dto.BoardResponse.PagedBoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.entity.BookMark;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.board.repository.BookMarkRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookMarkServiceTest {

    @Autowired private BookMarkService bookMarkService;
    @Autowired private BookMarkRepository bookMarkRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private GameRepository gameRepository;

    private User writer;
    private User viewer;
    private Board board;

    @BeforeEach
    void setUp() {
        // 기초 데이터 셋업
        Club club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build());

        writer = userRepository.save(createUser("writer@test.com", "writer", club));
        viewer = userRepository.save(createUser("viewer@test.com", "viewer", club));

        Game game = gameRepository.save(Game.builder()
                .homeClub(club)
                .awayClub(club)
                .gameStartDate(LocalDateTime.now().plusDays(1))
                .location("Gwangju")
                .build());

        board = boardRepository.save(Board.builder()
                .title("직관 모집")
                .content("같이 가요")
                .maxPerson(4)
                .currentPerson(1)
                .user(writer)
                .club(club)
                .game(game)
                .preferredGender("M")
                .preferredAgeRange("20s")
                .isCompleted(true)
                .liftUpDate(LocalDateTime.now())
                .build());
    }

    // 1. addBookMark 테스트
    @Test
    @DisplayName("찜하기 성공 - DB에 BookMark 데이터가 저장되어야 한다")
    void addBookMark_Success() {
        // when
        StateResponse response = bookMarkService.addBookMark(viewer.getId(), board.getId());

        // then
        assertThat(response.isState()).isTrue();

        // DB 검증
        boolean exists = bookMarkRepository.existsByUserAndBoardAndDeletedAtIsNull(viewer, board);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("자신의 게시글을 찜하면 예외가 발생한다")
    void addBookMark_Fail_SelfBookmark() {
        // when & then
        assertThatThrownBy(() -> bookMarkService.addBookMark(writer.getId(), board.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("본인 게시글은 찜할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 찜한 게시글을 다시 찜하면 예외가 발생한다")
    void addBookMark_Fail_Duplicate() {
        // given
        bookMarkService.addBookMark(viewer.getId(), board.getId());

        // when & then
        assertThatThrownBy(() -> bookMarkService.addBookMark(viewer.getId(), board.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("이미 찜한 게시글입니다.");
    }

    // 2. getBookMarkBoardList 테스트
    @Test
    @DisplayName("내가 찜한 게시글 목록 조회 성공")
    void getBookMarkBoardList_Success() {
        // given
        bookMarkService.addBookMark(viewer.getId(), board.getId());

        // when
        PagedBoardInfo result = bookMarkService.getBookMarkBoardList(viewer.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getBoardInfoList()).hasSize(1);
        assertThat(result.getBoardInfoList().get(0).getBoardId()).isEqualTo(board.getId());
        assertThat(result.getBoardInfoList().get(0).getTitle()).isEqualTo(board.getTitle());
    }

    // 3. removeBookMark 테스트
    @Test
    @DisplayName("찜 취소 성공 - 데이터가 Soft Delete 되어야 한다")
    void removeBookMark_Success() {
        // given
        bookMarkService.addBookMark(viewer.getId(), board.getId());

        // when
        StateResponse response = bookMarkService.removeBookMark(viewer.getId(), board.getId());

        // then
        assertThat(response.isState()).isTrue();

        // DB 검증 (Soft Delete 확인)
        boolean exists = bookMarkRepository.existsByUserAndBoardAndDeletedAtIsNull(viewer, board);
        assertThat(exists).isFalse();

        // 실제 데이터는 존재하지만 deletedAt이 설정되어 있어야 함 (Optional)
        BookMark deletedBookMark = bookMarkRepository.findAll().stream()
                .filter(bm -> bm.getUser().getId().equals(viewer.getId()) && bm.getBoard().getId().equals(board.getId()))
                .findFirst().orElseThrow();
        assertThat(deletedBookMark.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 찜을 취소하려 하면 예외가 발생한다")
    void removeBookMark_Fail_NotFound() {
        // when & then
        assertThatThrownBy(() -> bookMarkService.removeBookMark(viewer.getId(), board.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("존재하지 않는 찜입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 찜하기를 시도하면 예외가 발생한다")
    void addBookMark_Fail_UserNotFound() {
        // given
        Long invalidUserId = 999999L;

        // when & then
        assertThatThrownBy(() -> bookMarkService.addBookMark(invalidUserId, board.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 찜하기 시도하면 예외가 발생한다")
    void addBookMark_Fail_BoardNotFound() {
        // given
        Long invalidBoardId = 999999L;

        // when & then
        assertThatThrownBy(() -> bookMarkService.addBookMark(viewer.getId(), invalidBoardId))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.BOARD_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 찜 목록을 조회하면 예외가 발생한다")
    void getBookMarkBoardList_Fail_UserNotFound() {
        // given
        Long invalidUserId = 999999L;

        // when & then
        assertThatThrownBy(() -> bookMarkService.getBookMarkBoardList(invalidUserId, PageRequest.of(0, 10)))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 찜 취소를 시도하면 예외가 발생한다")
    void removeBookMark_Fail_UserNotFound() {
        // given
        Long invalidUserId = 999999L;

        // when & then
        assertThatThrownBy(() -> bookMarkService.removeBookMark(invalidUserId, board.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 찜 취소를 시도하면 예외가 발생한다")
    void removeBookMark_Fail_BoardNotFound() {
        // given
        Long invalidBoardId = 999999L;

        // when & then
        assertThatThrownBy(() -> bookMarkService.removeBookMark(viewer.getId(), invalidBoardId))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.BOARD_NOT_FOUND.getMessage());
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
