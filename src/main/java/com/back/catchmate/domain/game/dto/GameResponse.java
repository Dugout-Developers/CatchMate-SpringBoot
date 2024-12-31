package com.back.catchmate.domain.game.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class GameResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameInfo {
        @NotNull
        private Long homeClubId;
        @NotNull
        private Long awayClubId;
        @NotNull
        private String gameStartDate;
        @NotNull
        private String location;
    }
}
