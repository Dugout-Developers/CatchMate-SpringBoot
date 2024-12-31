package com.back.catchmate.domain.board.dto;

import com.back.catchmate.domain.game.dto.GameResponse.GameInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public abstract class BoardResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardInfo {
        private Long boardId;
        private String title;
        private String content;
        private Long cheerClubId;
        private int currentPerson;
        private int maxPerson;
        private String preferredGender;
        private String preferredAgeRange;
        private GameInfo gameInfo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardDeleteInfo {
        Long boardId;
        LocalDateTime deletedAt;
    }
}
