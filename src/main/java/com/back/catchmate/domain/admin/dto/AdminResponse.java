package com.back.catchmate.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
        private Map<String, Long> teamSupportCountMap;   // 구단별 가입자 수
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CheerStyleStatsInfo {
        private Map<String, Long> cheerStyleCountMap;    // 응원 스타일별 가입자 수
    }
}
