package com.back.catchmate.domain.board.converter;

import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import org.springframework.stereotype.Component;

@Component
public class BoardConverter {
    public BoardInfo toBoardInfo(Board board) {
        return BoardInfo.builder()
                .title(board.getTitle()).build();
    }
}
