package com.back.catchmate.domain.board.dto;

import com.back.catchmate.domain.game.dto.GameRequest.CreateGameRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public abstract class BoardRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBoardRequest {
        @NotNull
        private String title;
        @NotNull
        private String content;
        @NotNull
        private Long cheerClubId;
        @NotNull
        private String preferredGender;
        @NotNull
        private List<String> preferredAgeRange;
        @NotNull
        private CreateGameRequest gameRequest;
    }
}
