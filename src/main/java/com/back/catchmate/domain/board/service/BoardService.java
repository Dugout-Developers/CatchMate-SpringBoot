package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.dto.BoardRequest.CreateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardRequest.*;
import com.back.catchmate.domain.board.dto.BoardResponse;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardDeleteInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.PagedBoardInfo;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BoardService {
    BoardInfo createBoard(Long userId, CreateBoardRequest request);

    BoardInfo getBoard(Long userId, Long boardId);

    PagedBoardInfo getBoardList(Long userId, LocalDate localDate, Integer maxPerson, Long preferredTeamId, Pageable pageable);

    PagedBoardInfo getBoardListByUserId(Long loginUserId, Long userId, Pageable pageable);

    BoardDeleteInfo deleteBoard(Long userId, Long boardId);

    BoardInfo updateBoard(Long userId, Long boardId, UpdateBoardRequest boardRequest);
}
