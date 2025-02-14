package com.back.catchmate.domain.report.converter;

import com.back.catchmate.domain.report.dto.ReportRequest.CreateReportRequest;
import com.back.catchmate.domain.report.entity.Report;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReportConverter {
    public Report toEntity(User reporter, User reportedUser, CreateReportRequest request) {
        return Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reportType(request.getReportType())
                .content(request.getContent())
                .build();
    }
}
