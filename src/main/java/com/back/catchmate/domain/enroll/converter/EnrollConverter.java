package com.back.catchmate.domain.enroll.converter;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EnrollConverter {
    public Enroll toEntity(CreateEnrollRequest createEnrollRequest, User user, Board board) {
        return Enroll.builder()
                .user(user)
                .board(board)
                .acceptStatus(AcceptStatus.PENDING)
                .description(createEnrollRequest.getDescription())
                .isNew(true)
                .build();
    }

    public CreateEnrollInfo toCreateEnrollInfo(Enroll enroll) {
        return CreateEnrollInfo.builder()
                .enrollId(enroll.getId())
                .requestAt(enroll.getCreatedAt())
                .build();
    }

    public CancelEnrollInfo toCancelEnrollInfo(Enroll enroll) {
        return CancelEnrollInfo.builder()
                .enrollId(enroll.getId())
                .deletedAt(enroll.getUpdatedAt())
                .build();
    }
}
