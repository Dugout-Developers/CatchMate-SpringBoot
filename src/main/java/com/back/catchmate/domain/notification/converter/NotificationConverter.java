package com.back.catchmate.domain.notification.converter;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.notification.dto.NotificationResponse.NotificationInfo;
import com.back.catchmate.domain.notification.dto.NotificationResponse.PagedNotificationInfo;
import com.back.catchmate.domain.notification.entity.Notification;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationConverter {
    private final BoardConverter boardConverter;

    public Notification toEntity(User user, Board board, String senderProfileImageUrl, String title, String body, AcceptStatus acceptStatus) {
        return Notification.builder()
                .user(user)
                .board(board)
                .title(title)
                .body(body)
                .senderProfileImageUrl(senderProfileImageUrl)
                .isRead(false)
                .acceptStatus(acceptStatus)
                .build();
    }

    public PagedNotificationInfo toPagedNotificationInfo(Page<Notification> notificationList) {
        List<NotificationInfo> enrollRequestInfoList = notificationList.stream()
                .map(notification -> toNotificationInfo(notification, notification.getBoard()))
                .collect(Collectors.toList());

        return PagedNotificationInfo.builder()
                .notificationInfoList(enrollRequestInfoList)
                .totalPages(notificationList.getTotalPages())
                .totalElements(notificationList.getTotalElements())
                .isFirst(notificationList.isFirst())
                .isLast(notificationList.isLast())
                .build();
    }

    public NotificationInfo toNotificationInfo(Notification notification, Board board) {
        BoardInfo boardInfo = boardConverter.toBoardInfo(board, board.getGame());

        return NotificationInfo.builder()
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .senderProfileImageUrl(notification.getSenderProfileImageUrl())
                .isRead(notification.isRead())
                .acceptStatus(notification.getAcceptStatus())
                .boardInfo(boardInfo)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
