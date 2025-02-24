package com.back.catchmate.domain.admin.controller;

import com.back.catchmate.domain.admin.dto.AdminRequest.AnswerInquiryRequest;
import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.admin.dto.AdminResponse.AdminDashboardInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.BoardInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.GenderRatioDto;
import com.back.catchmate.domain.admin.dto.AdminResponse.InquiryInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedBoardInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedInquiryInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedUserInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.UserInfo;
import com.back.catchmate.domain.admin.service.AdminService;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.back.catchmate.domain.admin.dto.AdminResponse.CheerStyleStatsInfo;
import static com.back.catchmate.domain.admin.dto.AdminResponse.TeamSupportStatsInfo;

@Tag(name = "관리자 관련 API")
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

    @GetMapping("/user/team-support")
    @Operation(summary = "구단별 응원자 통계 조회", description = "각 구단별 응원자 수 통계를 조회하는 API 입니다.")
    public TeamSupportStatsInfo getTeamSupportStats() {
        return adminService.getTeamSupportStats();
    }

    @GetMapping("/user/cheer-style")
    @Operation(summary = "응원 스타일별 가입자 수 조회", description = "각 응원 스타일에 따른 가입자 수를 조회하는 API 입니다.")
    public CheerStyleStatsInfo getCheerStyleStats() {
        return adminService.getCheerStyleStats();
    }

    @GetMapping("/user")
    @Operation(summary = "유저 리스트 조회", description = "구단명을 쿼리 파라미터로 전달받아 해당 구단의 유저 정보 리스트를 조회하는 API 입니다.")
    public PagedUserInfo getUserInfoList(@RequestParam(required = false) String clubName,
                                         @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                         @Parameter(hidden = true) Pageable pageable) {
        return adminService.getUserInfoList(clubName, pageable);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "유저 상세정보 조회", description = "유저 상세정보를 조회하는 API 입니다.")
    public UserInfo getUserInfo(@PathVariable Long userId) {
        return adminService.getUserInfo(userId);
    }

    @GetMapping("/user/{userId}/board")
    @Operation(summary = "특정 유저의 게시글 리스트 조회", description = "특정 유저의 게시글 리스트를 조회하는 API 입니다.")
    public PagedBoardInfo getBoardInfoList(@PathVariable Long userId,
                                           @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                           @Parameter(hidden = true) Pageable pageable) {
        return adminService.getBoardInfoList(userId, pageable);
    }

    @GetMapping("/board/{boardId}")
    @Operation(summary = "특정 게시글 조회", description = "게시글 상세정보를 조회하는 API 입니다.")
    public BoardInfo getBoardInfo(@PathVariable Long boardId) {
        return adminService.getBoardInfo(boardId);
    }

    @GetMapping("/inquiry")
    @Operation(summary = "문의 내역 조회", description = "사용자들의 문의 내역을 페이징하여 조회하는 API 입니다.")
    public PagedInquiryInfo getInquiryList(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                           @Parameter(hidden = true) Pageable pageable) {
        return adminService.getInquiryList(pageable);
    }

    @GetMapping("/inquiry/{inquiryId}")
    @Operation(summary = "문의 내역 단일 조회", description = "특정 문의 내역의 상세 정보를 조회하는 API 입니다.")
    public InquiryInfo getInquiry(@PathVariable Long inquiryId) {
        return adminService.getInquiry(inquiryId);
    }

    @PatchMapping("/inquiry/{inquiryId}/answer")
    @Operation(summary = "문의 내역에 대한 답변", description = "관리자가 특정 문의에 대해 답변을 작성하는 API 입니다.")
    public StateResponse answerInquiry(@JwtValidation Long userId,
                                       @PathVariable Long inquiryId,
                                       @RequestBody AnswerInquiryRequest request) throws IOException {
        return adminService.answerInquiry(userId, inquiryId, request);
    }

    @GetMapping("/report")
    @Operation(summary = "신고 내역 조회", description = "신고 내역을 페이징하여 조회하는 API 입니다.")
    public AdminResponse.PagedReportInfo getReportList(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                       @Parameter(hidden = true) Pageable pageable) {
        return adminService.getReportList(pageable);
    }

    @GetMapping("/report/{reportId}")
    @Operation(summary = "신고 내역 단일 조회", description = "특정 신고 내역의 상세 정보를 조회하는 API 입니다.")
    public AdminResponse.ReportInfo getReport(@PathVariable Long reportId) {
        return adminService.getReport(reportId);
    }

    @PatchMapping("/report/{reportId}/process")
    @Operation(summary = "신고 처리", description = "특정 신고 내역을 처리하는 API 입니다.")
    public StateResponse processReport(@PathVariable Long reportId) {
        return adminService.processReport(reportId);
    }
}
