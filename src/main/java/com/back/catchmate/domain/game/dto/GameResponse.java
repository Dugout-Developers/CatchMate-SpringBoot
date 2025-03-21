package com.back.catchmate.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public abstract class GameResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameInfo {
        private Long homeClubId;
        private Long awayClubId;
        private LocalDateTime gameStartDate;
        private String location;
    }
}
