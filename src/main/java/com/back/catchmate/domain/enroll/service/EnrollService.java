package com.back.catchmate.domain.enroll.service;

import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.EnrollDescriptionInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.NewEnrollCountInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollRequestInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.UpdateEnrollInfo;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface EnrollService {
    CreateEnrollInfo requestEnroll(CreateEnrollRequest request, Long boardId, Long userId) throws IOException;

    CancelEnrollInfo cancelEnroll(Long enrollId, Long userId);

    PagedEnrollRequestInfo getRequestEnrollList(Long userId, Pageable pageable);

    PagedEnrollReceiveInfo getReceiveEnrollList(Long userId, Pageable pageable);

    PagedEnrollReceiveInfo getReceiveEnrollListByBoardId(Long userId, Long boardId, Pageable pageable);

    NewEnrollCountInfo getNewEnrollListCount(Long userId);

    UpdateEnrollInfo acceptEnroll(Long enrollId, Long userId) throws IOException;

    UpdateEnrollInfo rejectEnroll(Long enrollId, Long userId) throws IOException;

    EnrollDescriptionInfo getEnrollDescriptionById(Long enrollId, Long userId);
}
