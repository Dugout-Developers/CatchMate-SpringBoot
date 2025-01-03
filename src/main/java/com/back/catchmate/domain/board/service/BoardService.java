package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.dto.BoardResponse.BoardDeleteInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.PagedBoardInfo;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static com.back.catchmate.domain.board.dto.BoardRequest.*;

public interface BoardService {
    BoardInfo createOrUpdateBoard(Long userId, Long boardId, CreateOrUpdateBoardRequest request);

    BoardInfo getBoard(Long userId, Long boardId);

    PagedBoardInfo getBoardList(Long userId, LocalDate localDate, Integer maxPerson, Long preferredTeamId, Pageable pageable);

    PagedBoardInfo getBoardListByUserId(Long loginUserId, Long userId, Pageable pageable);

    BoardInfo getTempBoard(Long userId, Long boardId);

    BoardDeleteInfo deleteBoard(Long userId, Long boardId);

    BoardInfo updateLiftUpDate(Long userId, Long boardId);
}
