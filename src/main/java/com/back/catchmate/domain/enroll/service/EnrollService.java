package com.back.catchmate.domain.enroll.service;

import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollRequestInfo;
import org.springframework.data.domain.Pageable;

public interface EnrollService {
    CreateEnrollInfo requestEnroll(CreateEnrollRequest request, Long boardId, Long userId);

    CancelEnrollInfo cancelEnroll(Long enrollId, Long userId);

    PagedEnrollRequestInfo getRequestEnrollList(Long userId, Pageable pageable);

    PagedEnrollReceiveInfo getReceiveEnrollList(Long userId, Pageable pageable);

    PagedEnrollReceiveInfo getReceiveEnrollListByBoardId(Long userId, Long boardId, Pageable pageable);

}
