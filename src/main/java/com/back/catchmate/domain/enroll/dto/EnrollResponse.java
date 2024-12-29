package com.back.catchmate.domain.enroll.dto;

import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public abstract class EnrollResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollInfo {
        private Long enrollId;
        private AcceptStatus acceptStatus;
        private String description;
        private LocalDateTime requestDate;
        private boolean isNew;
        private UserInfo userInfo;
        private BoardInfo boardInfo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewEnrollCountInfo {
        private int newEnrollCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateEnrollInfo {
        private Long enrollId;
        private AcceptStatus acceptStatus;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateEnrollInfo {
        private Long enrollId;
        private LocalDateTime requestAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelEnrollInfo {
        private Long enrollId;
        private LocalDateTime deletedAt;
    }
}
