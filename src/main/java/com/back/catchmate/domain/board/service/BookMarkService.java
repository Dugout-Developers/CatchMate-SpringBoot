package com.back.catchmate.domain.board.service;

import com.back.catchmate.global.dto.StateResponse;
import org.springframework.data.domain.Pageable;

import static com.back.catchmate.domain.board.dto.BoardResponse.*;

public interface BookMarkService {
    StateResponse addBookMark(Long userId, Long boardId);

    PagedBoardInfo getBookMarkBoardList(Long userId, Pageable pageable);

    StateResponse removeBookMark(Long userId, Long boardId);
}
