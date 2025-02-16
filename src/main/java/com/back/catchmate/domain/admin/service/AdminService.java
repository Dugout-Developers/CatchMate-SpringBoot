package com.back.catchmate.domain.admin.service;

import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.admin.dto.AdminResponse.CheerStyleStatsInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.TeamSupportStatsInfo;

public interface AdminService {
    AdminResponse.AdminDashboardInfo getDashboardStats();

    AdminResponse.GenderRatioDto getGenderRatio();

    TeamSupportStatsInfo getTeamSupportStats();

    CheerStyleStatsInfo getCheerStyleStats();
}
