package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardRequest.CreateOrUpdateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardDeleteInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.PagedBoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.game.converter.GameConverter;
import com.back.catchmate.domain.game.dto.GameRequest.CreateGameRequest;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private static final Long DEFAULT_CLUB_ID = 0L;
    private static final Long DEFAULT_GAME_ID = 0L;
    private final BoardRepository boardRepository;
    private final GameRepository gameRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final BoardConverter boardConverter;
    private final GameConverter gameConverter;

    @Override
    @Transactional
    public BoardInfo createOrUpdateBoard(Long userId, Long boardId, CreateOrUpdateBoardRequest request) {
        // 유저 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // Cheer Club 조회
        Club cheerClub = (request.getCheerClubId() != 0)
                ? clubRepository.findById(request.getCheerClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND))
                : getDefaultClub();

        // Home Club 및 Away Club 조회
        Club homeClub = (request.getGameRequest().getHomeClubId() != 0)
                ? clubRepository.findById(request.getGameRequest().getHomeClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND))
                : getDefaultClub();

        Club awayClub = (request.getGameRequest().getAwayClubId() != 0)
                ? clubRepository.findById(request.getGameRequest().getAwayClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND))
                : getDefaultClub();

        // Game 조회 또는 생성
        Game game = findOrCreateGame(homeClub, awayClub, request.getGameRequest());
        Board board;

        if (boardId != null) {
            // 기존 Board 업데이트
            board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                    .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

            if (user.isDifferentUserFrom(board.getUser())) {
                throw new BaseException(ErrorCode.BOARD_UPDATE_BAD_REQUEST);
            }

            board.updateBoard(cheerClub, game, request);
        } else {
            // 새로운 Board 생성
            board = boardConverter.toEntity(user, game, cheerClub, request);
            boardRepository.save(board);
        }

        return boardConverter.toBoardInfo(board, game);
    }

    private Club getDefaultClub() {
        return clubRepository.findById(DEFAULT_CLUB_ID)
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));
    }

    private Game getDefaultGame() {
        return gameRepository.findById(DEFAULT_GAME_ID)
                .orElseThrow(() -> new BaseException(ErrorCode.GAME_NOT_FOUND));
    }

    private Game findOrCreateGame(Club homeClub, Club awayClub, CreateGameRequest createGameRequest) {
        Game game = gameRepository.findByHomeClubAndAwayClubAndGameStartDate(
                homeClub, awayClub, LocalDateTime.parse(createGameRequest.getGameStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        if (game != null) {
            game.setGameStartDate(LocalDateTime.parse(createGameRequest.getGameStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            game.setHomeClub(homeClub);
            game.setAwayClub(awayClub);
        } else {
            game = this.gameConverter.toEntity(homeClub, awayClub, createGameRequest);
            gameRepository.save(game);
        }

        return game;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardInfo getBoard(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        return boardConverter.toBoardInfo(board, board.getGame());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedBoardInfo getBoardList(Long userId, LocalDate gameStartDate, Integer maxPerson, List<Long> preferredTeamIdList, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Board> boardList = boardRepository.findFilteredBoards(gameStartDate, maxPerson, preferredTeamIdList, pageable);
        return boardConverter.toPagedBoardInfoFromBoardList(boardList);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedBoardInfo getBoardListByUserId(Long loginUserId, Long userId, Pageable pageable) {
        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Board> boardList = boardRepository.findAllByUserIdAndDeletedAtIsNullAndIsCompletedIsTrue(user.getId(), pageable);
        return boardConverter.toPagedBoardInfoFromBoardList(boardList);
    }

    @Override
    @Transactional
    public BoardInfo getTempBoard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board tempBoard = boardRepository.findTopByUserIdAndIsCompletedIsFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.TEMP_BOARD_NOT_FOUND));

        if (user.isDifferentUserFrom(tempBoard.getUser())) {
            throw new BaseException(ErrorCode.TEMP_BOARD_BAD_REQUEST);
        }

        return boardConverter.toBoardInfo(tempBoard, tempBoard.getGame());
    }

    @Override
    @Transactional
    public BoardDeleteInfo deleteBoard(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));


        if (user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(ErrorCode.BOARD_DELETE_BAD_REQUEST);
        }

        board.deleteBoard();
        return boardConverter.toBoardDeleteInfo(boardId);
    }

    @Override
    @Transactional
    public BoardInfo updateLiftUpDate(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        if (user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(ErrorCode.BOARD_LIFT_UP_BAD_REQUEST);
        }

        // note: 3일 간격으로 수정할 수 있음.
        if (board.getLiftUpDate().plusDays(3).isBefore(LocalDateTime.now())) {
            board.setLiftUpDate(LocalDateTime.now());
        } else {
            throw new BaseException(ErrorCode.BOARD_NOT_ALLOWED_UPDATE_LIFT_UPDATE);
        }

        return boardConverter.toBoardInfo(board, board.getGame());
    }
}
