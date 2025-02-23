package com.back.catchmate.domain.admin.service;

import com.back.catchmate.domain.admin.dto.AdminRequest;
import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.admin.dto.AdminResponse.CheerStyleStatsInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedBoardInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedUserInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.TeamSupportStatsInfo;
import com.back.catchmate.global.dto.StateResponse;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface AdminService {
    AdminResponse.AdminDashboardInfo getDashboardStats();

    AdminResponse.GenderRatioDto getGenderRatio();

    TeamSupportStatsInfo getTeamSupportStats();

    CheerStyleStatsInfo getCheerStyleStats();

    PagedUserInfo getUserInfoList(String clubName, Pageable pageable);

    AdminResponse.UserInfo getUserInfo(Long userId);

    PagedBoardInfo getBoardInfoList(Long userId, Pageable pageable);

    AdminResponse.BoardInfo getBoardInfo(Long boardId);

    AdminResponse.PagedInquiryInfo getInquiryList(Pageable pageable);

    AdminResponse.InquiryInfo getInquiry(Long inquiryId);

    StateResponse answerInquiry(Long userId, Long inquiryId, AdminRequest.AnswerInquiryRequest request) throws IOException;
}
