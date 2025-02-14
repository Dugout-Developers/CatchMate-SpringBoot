package com.back.catchmate.domain.report.converter;

import com.back.catchmate.domain.report.dto.ReportRequest;
import com.back.catchmate.domain.report.entity.Report;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReportConverter {
    public Report toEntity(User reporter, User reportedUser, ReportRequest.CreateReportRequest request) {
        return Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reportReason(request.getReportReason())
                .reasonDetail(request.getReasonDetail())
                .build();
    }
}
