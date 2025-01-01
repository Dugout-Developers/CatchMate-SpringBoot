package com.back.catchmate.domain.game.converter;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.game.dto.GameRequest.CreateGameRequest;
import com.back.catchmate.domain.game.dto.GameResponse.GameInfo;
import com.back.catchmate.domain.game.entity.Game;
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

    public GameInfo toGameInfo(Game game) {
        return GameInfo.builder()
                .homeClubId(game.getHomeClub().getId())
                .awayClubId(game.getAwayClub().getId())
                .gameStartDate(game.getGameStartDate())
                .location(game.getLocation())
                .build();
    }
}
