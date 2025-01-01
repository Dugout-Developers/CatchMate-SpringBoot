package com.back.catchmate.domain.board.service;

import com.back.catchmate.global.dto.StateResponse;

public interface BookMarkService {
    StateResponse addBookMark(Long userId, Long boardId);

    StateResponse removeBookMark(Long userId, Long boardId);
}
