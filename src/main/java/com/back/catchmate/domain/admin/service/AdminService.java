package com.back.catchmate.domain.admin.service;

import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.admin.dto.AdminResponse.CheerStyleStatsInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedBoardInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedUserInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.TeamSupportStatsInfo;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    AdminResponse.AdminDashboardInfo getDashboardStats();

    AdminResponse.GenderRatioDto getGenderRatio();

    TeamSupportStatsInfo getTeamSupportStats();

    CheerStyleStatsInfo getCheerStyleStats();

    PagedUserInfo getUserInfoList(String clubName, Pageable pageable);

    AdminResponse.UserInfo getUserInfo(Long userId);

    PagedBoardInfo getBoardInfoList(Long userId, Pageable pageable);

    AdminResponse.BoardInfo getBoardInfo(Long boardId);
}
