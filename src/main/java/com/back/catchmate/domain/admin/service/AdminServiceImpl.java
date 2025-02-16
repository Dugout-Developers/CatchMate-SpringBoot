package com.back.catchmate.domain.admin.service;

import com.back.catchmate.domain.admin.converter.AdminConverter;
import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.inquiry.repository.InquiryRepository;
import com.back.catchmate.domain.report.repository.ReportRepository;
import com.back.catchmate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReportRepository reportRepository;
    private final InquiryRepository inquiryRepository;
    private final AdminConverter adminConverter;

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.AdminDashboardInfo getDashboardStats() {
        long totalUserCount = userRepository.countByDeletedAtIsNull();
        long totalBoardCount = boardRepository.countByDeletedAtIsNullAndIsCompletedIsTrue();
        long totalReportCount = reportRepository.countByDeletedAtIsNull();
        long totalInquiryCount = inquiryRepository.countByDeletedAtIsNull();

        return adminConverter.toAdminDashboardInfo(totalUserCount, totalBoardCount, totalReportCount, totalInquiryCount);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.GenderRatioDto getGenderRatio() {
        long maleCount = userRepository.countByGenderAndDeletedAtIsNull('M');
        long femaleCount = userRepository.countByGenderAndDeletedAtIsNull('F');
        long totalCount = maleCount + femaleCount;

        if (totalCount == 0) {
            return adminConverter.toGenderRatioDto(0.0, 0.0);// 사용자가 없을 경우
        }

        double maleRatio = Math.round(((double) maleCount / totalCount * 100) * 10) / 10.0;
        double femaleRatio = Math.round(((double) femaleCount / totalCount * 100) * 10) / 10.0;

        return adminConverter.toGenderRatioDto(maleRatio, femaleRatio);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.TeamSupportStatsInfo getTeamSupportStats() {
        // 구단별 가입자 수를 조회
        List<Object[]> results = userRepository.countUsersByClub();
        // 구단별 가입자 수를 Map으로 변환
        Map<String, Long> teamSupportCountMap = createTeamOrCheerStyleSupportCountMap(results);

        initializeKboTeamsWithZero(teamSupportCountMap);
        return adminConverter.toTeamSupportStatsInfo(teamSupportCountMap);
    }

    @Override
    public AdminResponse.CheerStyleStatsInfo getCheerStyleStats() {
        // 응원 스타일별 가입자 수를 조회
        List<Object[]> results = userRepository.countUsersByWatchStyle();
        // 응원 스타일별 가입자 수를 Map으로 변환
        Map<String, Long> cheerStyleSupportCountMap = createTeamOrCheerStyleSupportCountMap(results);

        initializeRolesWithZero(cheerStyleSupportCountMap);
        return adminConverter.toCheerStyleStatsInfo(cheerStyleSupportCountMap);
    }

    private Map<String, Long> createTeamOrCheerStyleSupportCountMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0], // 구단명 (clubName)
                        result -> (Long) result[1]    // 가입자 수 (count)
                ));
    }

    private void initializeKboTeamsWithZero(Map<String, Long> teamSupportCountMap) {
        List<String> kboTeams = Arrays.asList(
                "삼성 라이온즈", "LG 트윈스", "KIA 타이거즈", "두산 베어스", "롯데 자이언츠",
                "한화 이글스", "SK 와이번스", "NC 다이노스", "키움 히어로즈", "SSG 랜더스"
        );

        // 각 팀에 대해 존재하지 않으면 0으로 초기화
        kboTeams.forEach(team -> teamSupportCountMap.putIfAbsent(team, 0L));
    }

    private void initializeRolesWithZero(Map<String, Long> roleSupportCountMap) {
        List<String> roles = Arrays.asList(
                "감독", "어미새", "응원단장", "먹보", "돌하르방", "보살"
        );

        // 각 역할에 대해 존재하지 않으면 0으로 초기화
        roles.forEach(role -> roleSupportCountMap.putIfAbsent(role, 0L));
    }
}
