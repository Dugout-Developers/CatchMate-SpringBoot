package com.back.catchmate.domain.report.service;

import com.back.catchmate.domain.report.dto.ReportRequest.CreateReportRequest;
import com.back.catchmate.global.dto.StateResponse;

public interface ReportService {
    StateResponse reportUser(Long reporterId, CreateReportRequest request);
}
