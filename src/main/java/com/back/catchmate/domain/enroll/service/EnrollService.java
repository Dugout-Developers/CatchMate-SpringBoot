package com.back.catchmate.domain.enroll.service;

import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;

import java.io.IOException;

public interface EnrollService {
    CreateEnrollInfo createEnroll(CreateEnrollRequest request, Long boardId, Long userId) throws IOException;

    CancelEnrollInfo cancelEnroll(Long enrollId, Long userId);
}
