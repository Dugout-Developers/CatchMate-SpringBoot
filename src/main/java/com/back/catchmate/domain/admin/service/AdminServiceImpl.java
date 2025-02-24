package com.back.catchmate.domain.admin.service;

import com.back.catchmate.domain.admin.converter.AdminConverter;
import com.back.catchmate.domain.admin.dto.AdminRequest;
import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.admin.dto.AdminResponse.InquiryInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedInquiryInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedUserInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.inquiry.entity.Inquiry;
import com.back.catchmate.domain.inquiry.repository.InquiryRepository;
import com.back.catchmate.domain.notification.service.FCMService;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.domain.report.entity.Report;
import com.back.catchmate.domain.report.repository.ReportRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.back.catchmate.domain.notification.message.NotificationMessages.INQUIRY_ANSWER_BODY;
import static com.back.catchmate.domain.notification.message.NotificationMessages.INQUIRY_ANSWER_TITLE;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final FCMService fcmService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReportRepository reportRepository;
    private final InquiryRepository inquiryRepository;
    private final UserChatRoomRepository userChatRoomRepository;
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
        Map<Long, Long> teamSupportCountMap = createCheerClubCountMap(results);

        initializeKboClubsWithZero(teamSupportCountMap);
        return adminConverter.toCheerClubStatsInfo(teamSupportCountMap);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.CheerStyleStatsInfo getCheerStyleStats() {
        // 응원 스타일별 가입자 수를 조회
        List<Object[]> results = userRepository.countUsersByWatchStyle();
        // 응원 스타일별 가입자 수를 Map으로 변환
        Map<String, Long> cheerStyleSupportCountMap = createCheerStyleCountMap(results);

        initializeRolesWithZero(cheerStyleSupportCountMap);
        return adminConverter.toCheerStyleStatsInfo(cheerStyleSupportCountMap);
    }

    private Map<Long, Long> createCheerClubCountMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));
    }

    private Map<String, Long> createCheerStyleCountMap(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    private void initializeKboClubsWithZero(Map<Long, Long> teamSupportCountMap) {
        List<Long> kboTeamIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L);

        // 각 팀에 대해 존재하지 않으면 0으로 초기화
        kboTeamIds.forEach(teamId -> teamSupportCountMap.putIfAbsent(teamId, 0L));
    }

    private void initializeRolesWithZero(Map<String, Long> roleSupportCountMap) {
        List<String> roles = Arrays.asList(
                "감독", "어미새", "응원단장", "먹보", "돌하르방", "보살"
        );

        // 각 역할에 대해 존재하지 않으면 0으로 초기화
        roles.forEach(role -> roleSupportCountMap.putIfAbsent(role, 0L));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedUserInfo getUserInfoList(String clubName, Pageable pageable) {
        Page<User> userList;
        if (clubName == null) {
            userList = userRepository.findAllByDeletedAtIsNull(pageable);
        } else {
            userList = userRepository.findByClubNameAndDeletedAtIsNull(clubName, pageable);
        }

        return adminConverter.toPagedUserInfo(userList);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.UserInfo getUserInfo(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return adminConverter.toUserInfo(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.PagedBoardInfo getBoardInfoList(Long userId, Pageable pageable) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Board> boardList = boardRepository.findAllByUserIdAndDeletedAtIsNullAndIsCompletedIsTrue(user.getId(), pageable);
        return adminConverter.toPagedBoardInfo(boardList);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.BoardInfo getBoardInfo(Long boardId) {
        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        List<UserChatRoom> userChatRoomList = userChatRoomRepository.findByChatRoomIdAndDeletedAtIsNull(board.getChatRoom().getId());

        return adminConverter.toBoardInfo(board, board.getGame(), userChatRoomList);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedInquiryInfo getInquiryList(Pageable pageable) {
        Page<Inquiry> inquiryList = inquiryRepository.findAll(pageable);

        return adminConverter.toPagedInquiryInfo(inquiryList);
    }

    @Override
    @Transactional(readOnly = true)
    public InquiryInfo getInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BaseException(ErrorCode.INQUIRY_NOT_FOUND));

        return adminConverter.toInquiryInfo(inquiry);
    }

    @Override
    @Transactional
    public StateResponse answerInquiry(Long userId, Long inquiryId, AdminRequest.AnswerInquiryRequest request) throws IOException {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BaseException(ErrorCode.INQUIRY_NOT_FOUND));

        inquiry.updateAnswer(request.getAnswer(), user);

        String title = INQUIRY_ANSWER_TITLE;
        String body = INQUIRY_ANSWER_BODY;

        fcmService.sendMessageByToken(inquiry.getUser().getFcmToken(), title, body, inquiryId);
        notificationService.createNotification(title, body, null, inquiryId, userId);

        return new StateResponse(true);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.PagedReportInfo getReportList(Pageable pageable) {
        Page<Report> reportList = reportRepository.findAll(pageable);

        return adminConverter.toPagedReportInfo(reportList);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse.ReportInfo getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND));

        return adminConverter.toReportInfo(report);
    }

    @Override
    @Transactional
    public StateResponse processReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND));

        report.updateReport();
        return new StateResponse(true);
    }
}
