package com.back.catchmate.domain.admin.dto;

import com.back.catchmate.domain.club.dto.ClubResponse;
import com.back.catchmate.domain.game.dto.GameResponse.GameInfo;
import com.back.catchmate.domain.inquiry.entity.InquiryType;
import com.back.catchmate.domain.report.entity.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public abstract class AdminResponse {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class AdminDashboardInfo {
        private long totalUserCount;  // 전체 유저 수
        private long totalBoardCount;  // 전체 게시글 수
        private long totalReportCount;  // 전체 유저 신고 수
        private long totalInquiryCount; // 전체 유저 문의 수
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class GenderRatioDto {
        private double maleRatio;
        private double femaleRatio;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TeamSupportStatsInfo {
        private Map<Long, Long> teamSupportCountMap;   // 구단별 가입자 수
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CheerStyleStatsInfo {
        private Map<String, Long> cheerStyleCountMap;    // 응원 스타일별 가입자 수
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String profileImageUrl;
        private String nickName;
        private ClubResponse.ClubInfo clubInfo;
        private char gender;
        private String email;
        private String socialType;
        private LocalDateTime joinedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PagedUserInfo {
        private List<UserInfo> userInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

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
        private AdminResponse.UserInfo userInfo;
        private List<UserInfo> userInfoList;
    }

    @Getter
    @Builder
    @AllArgsConstructor
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
    public static class InquiryInfo {
        private Long inquiryId;
        private InquiryType inquiryType;
        private String content;
        private String nickName;
        private String answer;
        private Boolean isCompleted;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PagedInquiryInfo {
        private List<InquiryInfo> inquiryInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ReportInfo {
        private Long reportId;
        private UserInfo reporter;
        private UserInfo reportedUser;
        private ReportType reportType;
        private String content;
        private Boolean isProcessed;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PagedReportInfo {
        private List<ReportInfo> reportInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeInfo {
        private Long noticeId;
        private String title;
        private String content;
        private AdminResponse.UserInfo userInfo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PagedNoticeInfo {
        private List<NoticeInfo> notices;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }
}
