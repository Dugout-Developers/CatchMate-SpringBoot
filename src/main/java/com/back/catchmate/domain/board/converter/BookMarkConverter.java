package com.back.catchmate.domain.board.converter;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.entity.BookMark;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BookMarkConverter {
    public BookMark toEntity(User user, Board board) {
        return BookMark.builder()
                .user(user)
                .board(board)
                .build();
    }
}
