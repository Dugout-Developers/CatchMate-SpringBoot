package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardRequest;
import com.back.catchmate.domain.board.dto.BoardRequest.*;
import com.back.catchmate.domain.board.dto.BoardResponse.*;
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

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final GameRepository gameRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final BoardConverter boardConverter;
    private final GameConverter gameConverter;

    @Override
    @Transactional
    public BoardInfo createBoard(Long userId, CreateBoardRequest createBoardRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Club cheerClub = clubRepository.findById(createBoardRequest.getCheerClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Club homeClub = clubRepository.findById(createBoardRequest.getGameRequest().getHomeClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Club awayClub = clubRepository.findById(createBoardRequest.getGameRequest().getAwayClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Game game = findOrCreateGame(homeClub, awayClub, createBoardRequest.getGameRequest());

        Board board = boardConverter.toEntity(user, game, cheerClub, createBoardRequest);
        this.boardRepository.save(board);

        return boardConverter.toBoardInfo(board, game);
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

        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        return boardConverter.toBoardInfo(board, board.getGame());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedBoardInfo getBoardList(Long userId, LocalDate gameStartDate, Integer maxPerson, Long preferredTeamId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Board> boardList = boardRepository.findFilteredBoards(gameStartDate, maxPerson, preferredTeamId, pageable);
        return boardConverter.toPagedBoardInfo(boardList);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedBoardInfo getBoardListByUserId(Long loginUserId, Long userId, Pageable pageable) {
        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Board> boardList = boardRepository.findAllByUserIdAndDeletedAtIsNull(user.getId(), pageable);
        return boardConverter.toPagedBoardInfo(boardList);
    }

    @Override
    @Transactional
    public BoardDeleteInfo deleteBoard(Long userId, Long boardId) {
        int updatedRows = boardRepository.softDeleteByUserIdAndBoardId(userId, boardId);

        if (updatedRows == 0) {
            throw new BaseException(ErrorCode.BOARD_NOT_FOUND);
        }

        return boardConverter.toBoardDeleteInfo(boardId);
    }

    @Override
    @Transactional
    public BoardInfo updateBoard(Long userId, Long boardId, UpdateBoardRequest updateBoardRequest) {
        Board board = this.boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(ErrorCode.BOARD_BAD_REQUEST);
        }

        Club cheerClub = this.clubRepository.findById(updateBoardRequest.getCheerClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Club homeClub = this.clubRepository.findById(updateBoardRequest.getGameRequest().getHomeClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Club awayClub = this.clubRepository.findById(updateBoardRequest.getGameRequest().getAwayClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Game game = this.findOrCreateGame(homeClub, awayClub, updateBoardRequest.getGameRequest());

        board.updateBoard(cheerClub, game, updateBoardRequest);
        return boardConverter.toBoardInfo(board, game);
    }
}
