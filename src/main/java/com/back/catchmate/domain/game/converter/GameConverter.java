package com.back.catchmate.domain.game.converter;

import com.back.catchmate.domain.board.dto.BoardRequest.CreateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.game.dto.GameRequest;
import com.back.catchmate.domain.game.dto.GameRequest.CreateGameRequest;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.user.entity.User;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class GameConverter {
    public Game toEntity(Club homeClub, Club awayClub, CreateGameRequest createGameRequest) {
        return Game.builder()
                .homeClub(homeClub)
                .awayClub(awayClub)
                .gameStartDate(LocalDateTime.parse(createGameRequest.getGameStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(createGameRequest.getLocation())
                .build();
    }
}
