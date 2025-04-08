package com.back.catchmate.domain.board.dto;

import com.back.catchmate.domain.game.dto.GameRequest.CreateGameRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.util.List;

public abstract class BoardRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrUpdateBoardRequest {
        @NotEmpty
        private String title;
        @NotEmpty
        private String content;
        @Positive
        @Range(min = 0, max = 8)
        private int maxPerson;
        @NotNull
        private Long cheerClubId;
        @NotEmpty
        private String preferredGender;
        @NotNull
        private List<String> preferredAgeRange;
        @NotNull
        private CreateGameRequest gameRequest;
        @NotNull
        private Boolean isCompleted;
    }
}
