package com.back.catchmate.domain.admin.controller;

import com.back.catchmate.domain.admin.dto.AdminResponse.AdminDashboardInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.GenderRatioDto;
import com.back.catchmate.domain.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.back.catchmate.domain.admin.dto.AdminResponse.CheerStyleStatsInfo;
import static com.back.catchmate.domain.admin.dto.AdminResponse.TeamSupportStatsInfo;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "대시보드 통계 조회", description = "관리자 대시보드에 필요한 통계 정보를 조회하는 API 입니다.")
    public AdminDashboardInfo getDashboardStats() {
        return adminService.getDashboardStats();
    }

    @GetMapping("/user/gender-ratio")
    @Operation(summary = "성비 통계 조회", description = "성별 비율을 조회하여 관리자에게 성비 통계를 제공하는 API 입니다.")
    public GenderRatioDto getGenderRatio() {
        return adminService.getGenderRatio();
    }

    @GetMapping("/users/team-support")
    @Operation(summary = "구단별 응원자 통계 조회", description = "각 구단별 응원자 수 통계를 조회하는 API 입니다.")
    public TeamSupportStatsInfo getTeamSupportStats() {
        return adminService.getTeamSupportStats();
    }

    @GetMapping("/users/cheer-style")
    @Operation(summary = "응원 스타일별 가입자 수 조회", description = "각 응원 스타일에 따른 가입자 수를 조회하는 API 입니다.")
    public CheerStyleStatsInfo getCheerStyleStats() {
        return adminService.getCheerStyleStats();
    }
}
