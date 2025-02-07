package com.back.catchmate.domain.enroll.dto;

import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public abstract class EnrollResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollRequestInfo {
        private Long enrollId;
        private AcceptStatus acceptStatus;
        private String description;
        private LocalDateTime requestDate;
        private UserInfo userInfo;
        private BoardInfo boardInfo;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedEnrollRequestInfo {
        private List<EnrollRequestInfo> enrollInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollReceiveInfo {
        private BoardInfo boardInfo;
        private List<EnrollInfo> enrollReceiveInfoList;
    }

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
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedEnrollReceiveInfo {
        private List<EnrollReceiveInfo> enrollInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
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
