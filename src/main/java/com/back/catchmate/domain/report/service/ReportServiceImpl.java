package com.back.catchmate.domain.report.service;

import com.back.catchmate.domain.report.converter.ReportConverter;
import com.back.catchmate.domain.report.dto.ReportRequest;
import com.back.catchmate.domain.report.entity.Report;
import com.back.catchmate.domain.report.repository.ReportRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportConverter reportConverter;

    @Override
    @Transactional
    public StateResponse reportUser(Long reporterId, ReportRequest.CreateReportRequest request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Report report = reportConverter.toEntity(reporter, reportedUser, request);
        reportRepository.save(report);

        return new StateResponse(true);
    }
}
