package com.back.catchmate.domain.board.converter;

import com.back.catchmate.domain.board.dto.BoardRequest.CreateOrUpdateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardDeleteInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.LiftUpStatusInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.PagedBoardInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.TempBoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.entity.BookMark;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.game.converter.GameConverter;
import com.back.catchmate.domain.game.dto.GameResponse.GameInfo;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoardConverter {
    private final BoardRepository boardRepository;
    private final GameConverter gameConverter;
    private final UserConverter userConverter;

    public Board toEntity(User user, Game game, Club cheerClub, CreateOrUpdateBoardRequest boardRequest) {
        return Board.builder()
                .title(boardRequest.getTitle())
                .content(boardRequest.getContent())
                .maxPerson(boardRequest.getMaxPerson())
                .currentPerson(1)
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

    public BoardInfo toBoardInfo(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        // GameInfo 및 UserInfo 변환
        GameInfo gameInfo = gameConverter.toGameInfo(board.getGame());
        UserInfo userInfo = userConverter.toUserInfo(board.getUser());

        // BoardInfo 객체 생성
        return BoardInfo.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .maxPerson(board.getMaxPerson())
                .cheerClubId(board.getClub().getId())
                .preferredGender(board.getPreferredGender())
                .preferredAgeRange(board.getPreferredAgeRange())
                .liftUpDate(board.getLiftUpDate())
                .gameInfo(gameInfo)
                .userInfo(userInfo)
                .chatRoomId(board.getChatRoom().getId())
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
                .liftUpDate(board.getLiftUpDate())
                .gameInfo(gameInfo)
                .userInfo(userInfo)
                .build();
    }

    public BoardInfo toBoardInfo(Board board, Game game, boolean isBookMarked, String buttonStatus) {
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
                .liftUpDate(board.getLiftUpDate())
                .isBookMarked(isBookMarked)
                .buttonStatus(buttonStatus)
                .gameInfo(gameInfo)
                .userInfo(userInfo)
                .chatRoomId(board.getChatRoom().getId())
                .build();
    }

    public TempBoardInfo toTempBoardInfo(Board board, Game game) {
        GameInfo gameInfo = gameConverter.toGameInfo(game);
        UserInfo userInfo = userConverter.toUserInfo(board.getUser());

        return TempBoardInfo.builder()
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

    public LiftUpStatusInfo toLiftUpStatusInfo(boolean state, String remainingTime) {
        return LiftUpStatusInfo.builder()
                .state(state)
                .remainTime(remainingTime)
                .build();
    }
}
