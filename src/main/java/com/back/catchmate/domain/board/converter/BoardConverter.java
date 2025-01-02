package com.back.catchmate.domain.board.converter;

import com.back.catchmate.domain.board.dto.BoardRequest.*;
import com.back.catchmate.domain.board.dto.BoardResponse.*;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.entity.BookMark;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.game.converter.GameConverter;
import com.back.catchmate.domain.game.dto.GameResponse.GameInfo;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserResponse;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoardConverter {
    private final GameConverter gameConverter;
    private final UserConverter userConverter;

    public Board toEntity(User user, Game game, Club cheerClub, CreateOrUpdateBoardRequest boardRequest) {
        return Board.builder()
                .title(boardRequest.getTitle())
                .content(boardRequest.getContent())
                .user(user)
                .club(cheerClub)
                .game(game)
                .preferredGender(boardRequest.getPreferredGender())
                .preferredAgeRange(String.join(",", boardRequest.getPreferredAgeRange()))
                .isCompleted(boardRequest.getIsCompleted())
                .liftUpDate(LocalDateTime.now())
                .build();
    }

    public PagedBoardInfo toPagedBoardInfoFromBoardList(Page<Board> boardList) {
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
        UserInfo userInfo = userConverter.toUserInfo(board.getUser());

        return BoardInfo.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .maxPerson(board.getMaxPerson())
                .cheerClubId(board.getClub().getId())
                .preferredGender(board.getPreferredGender())
                .preferredAgeRange(board.getPreferredAgeRange())
                .gameInfo(gameInfo)
                .liftUpDate(board.getLiftUpDate())
                .userInfo(userInfo)
                .build();
    }

    public BoardDeleteInfo toBoardDeleteInfo(Long boardId) {
        return BoardDeleteInfo.builder()
                .boardId(boardId)
                .deletedAt(LocalDateTime.now())
                .build();
    }

    public PagedBoardInfo toPagedBoardInfoFromBookMarkList(Page<BookMark> bookMarkList) {
        List<Board> boards = bookMarkList.stream()
                .map(BookMark::getBoard)
                .toList();

        Page<Board> boardPage = new PageImpl<>(boards, bookMarkList.getPageable(), bookMarkList.getTotalElements());
        return toPagedBoardInfoFromBoardList(boardPage);
    }
}
