package com.back.catchmate.domain.board.converter;

import com.back.catchmate.domain.board.dto.BoardRequest;
import com.back.catchmate.domain.board.dto.BoardRequest.CreateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.*;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BoardConverter {
    public Board toEntity(User user, Game game, Club cheerClub, CreateBoardRequest createBoardRequest) {
        return Board.builder()
                .title(createBoardRequest.getTitle())
                .content(createBoardRequest.getContent())
                .user(user)
                .club(cheerClub)
                .game(game)
                .preferredGender(createBoardRequest.getPreferredGender())
                .preferredAgeRange(String.join(",", createBoardRequest.getPreferredAgeRange()))
                .build();
    }

    public BoardInfo toBoardInfo(Board board) {
        return BoardInfo.builder()
                .title(board.getTitle())
                .content(board.getContent())
                .build();
    }

    public BoardDeleteInfo toBoardDeleteInfo(Long boardId) {
        return BoardDeleteInfo.builder()
                .boardId(boardId)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
