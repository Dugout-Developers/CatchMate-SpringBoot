package com.back.catchmate.domain.enroll.event;

import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EnrollmentRejectedEvent {
    private final String fcmToken;
    private final String title;
    private final String body;
    private final Long boardId;
    private final AcceptStatus acceptStatus;
    private final Long receiverId;
    private final Long senderId;
}
