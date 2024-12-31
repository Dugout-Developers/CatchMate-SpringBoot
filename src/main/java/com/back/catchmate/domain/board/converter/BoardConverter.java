package com.back.catchmate.domain.board.converter;

import com.back.catchmate.domain.board.dto.BoardRequest.CreateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.*;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.game.converter.GameConverter;
import com.back.catchmate.domain.game.dto.GameResponse;
import com.back.catchmate.domain.game.dto.GameResponse.GameInfo;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BoardConverter {
    private final GameConverter gameConverter;

    public Board toEntity(User user, Game game, Club cheerClub, CreateBoardRequest createBoardRequest) {
        return Board.builder()
                .title(createBoardRequest.getTitle())
                .content(createBoardRequest.getContent())
                .maxPerson(createBoardRequest.getMaxPerson())
                .user(user)
                .club(cheerClub)
                .game(game)
                .preferredGender(createBoardRequest.getPreferredGender())
                .preferredAgeRange(String.join(",", createBoardRequest.getPreferredAgeRange()))
                .build();
    }

    public PagedBoardInfo toPagedBoardInfo(Page<Board> boardList) {
        List<BoardInfo> boardInfoList = boardList.stream()
                .map(board -> toBoardInfo(board, board.getGame()))
                .toList();

        return PagedBoardInfo.builder()
                .boardInfoList(boardInfoList)
                .totalPages(boardList.getTotalPages())
                .totalElements(boardList.getTotalElements())
                .isFirst(boardList.isFirst())
                .isLast(boardList.isLast())
                .build();
    }

    public BoardInfo toBoardInfo(Board board, Game game) {
        GameInfo gameInfo = gameConverter.toGameInfo(game);

        return BoardInfo.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .maxPerson(board.getMaxPerson())
                .cheerClubId(board.getClub().getId())
                .preferredGender(board.getPreferredGender())
                .preferredAgeRange(board.getPreferredAgeRange())
                .gameInfo(gameInfo)
                .build();
    }

    public BoardDeleteInfo toBoardDeleteInfo(Long boardId) {
        return BoardDeleteInfo.builder()
                .boardId(boardId)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
