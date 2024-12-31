package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.dto.BoardRequest.CreateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;

public interface BoardService {
    BoardInfo createBoard(Long userId, CreateBoardRequest boardRequest);
}
