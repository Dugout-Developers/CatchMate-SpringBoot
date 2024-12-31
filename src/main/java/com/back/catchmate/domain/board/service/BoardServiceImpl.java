package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardRequest.CreateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardConverter boardConverter;
    private final GameConverter gameConverter;

    private final BoardRepository boardRepository;
    private final GameRepository gameRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BoardInfo createBoard(Long userId, CreateBoardRequest createBoardRequest) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Club cheerClub = this.clubRepository.findById(createBoardRequest.getCheerClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Club homeClub = this.clubRepository.findById(createBoardRequest.getGameRequest().getHomeClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Club awayClub = this.clubRepository.findById(createBoardRequest.getGameRequest().getAwayClubId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));

        Game game = this.findOrCreateGame(homeClub, awayClub, createBoardRequest.getGameRequest());


        Board board = boardConverter.toEntity(user, game, cheerClub, createBoardRequest);
        this.boardRepository.save(board);

        return boardConverter.toBoardInfo(board);
    }

    private Game findOrCreateGame(Club homeClub, Club awayClub, CreateGameRequest createGameRequest) {
        Game game = gameRepository.findByHomeClubAndAwayClubAndGameStartDate(
                homeClub, awayClub, LocalDateTime.parse(createGameRequest.getGameStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        if (game == null) {
            game = this.gameConverter.toEntity(homeClub, awayClub, createGameRequest);
            gameRepository.save(game);
        }
        return game;
    }
}
