package com.back.catchmate.domain.admin.converter;

import com.back.catchmate.domain.admin.dto.AdminResponse.AdminDashboardInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.CheerStyleStatsInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.back.catchmate.domain.admin.dto.AdminResponse.GenderRatioDto;
import static com.back.catchmate.domain.admin.dto.AdminResponse.TeamSupportStatsInfo;

@Component
public class AdminConverter {
    public AdminDashboardInfo toAdminDashboardInfo(long totalUserCount,
                                                   long totalBoardCount,
                                                   long totalReportCount,
                                                   long totalInquiryCount) {
        return AdminDashboardInfo.builder()
                .totalUserCount(totalUserCount)
                .totalBoardCount(totalBoardCount)
                .totalReportCount(totalReportCount)
                .totalInquiryCount(totalInquiryCount)
                .build();
    }

    public GenderRatioDto toGenderRatioDto(double maleRatio, double femaleRatio) {
        return GenderRatioDto.builder()
                .maleRatio(maleRatio)
                .femaleRatio(femaleRatio)
                .build();
    }

    public TeamSupportStatsInfo toTeamSupportStatsInfo(Map<String, Long> teamSupportCountMap) {
        return TeamSupportStatsInfo.builder()
                .teamSupportCountMap(teamSupportCountMap)
                .build();
    }

    public CheerStyleStatsInfo toCheerStyleStatsInfo(Map<String, Long> cheerStyleSupportCountMap) {
        return CheerStyleStatsInfo.builder()
                .cheerStyleCountMap(cheerStyleSupportCountMap)
                .build();
    }
}
