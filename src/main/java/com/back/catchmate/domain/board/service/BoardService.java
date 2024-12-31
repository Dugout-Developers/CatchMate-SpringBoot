package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.dto.BoardRequest.CreateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardDeleteInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;

public interface BoardService {
    BoardInfo createBoard(Long userId, CreateBoardRequest boardRequest);

    BoardInfo getBoard(Long userId, Long boardId);

    BoardDeleteInfo deleteBoard(Long userId, Long boardId);
}
