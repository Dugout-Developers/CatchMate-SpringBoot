package com.back.catchmate.domain.board.dto;

import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.domain.game.dto.GameResponse.GameInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
        private LocalDateTime liftUpDate;
        private GameInfo gameInfo;
        private UserInfo userInfo;
        private boolean isBookMarked;
        private String buttonStatus;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TempBoardInfo {
        private Long boardId;
        private String title;
        private String content;
        private Long cheerClubId;
        private int currentPerson;
        private int maxPerson;
        private String preferredGender;
        private String preferredAgeRange;
        private LocalDateTime liftUpDate;
        private GameInfo gameInfo;
        private UserInfo userInfo;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedBoardInfo {
        private List<BoardInfo> boardInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardDeleteInfo {
        Long boardId;
        LocalDateTime deletedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiftUpStatusInfo {
        private boolean state;
        private String remainTime;
    }
}
