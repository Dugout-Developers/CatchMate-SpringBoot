package com.back.catchmate.domain.notification.repository;

import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserIdAndDeletedAtIsNull(Long notificationId, Long userId);

    Optional<Notification> findByUserIdAndBoardIdAndAcceptStatusAndDeletedAtIsNull(Long userId, Long boardId, AcceptStatus acceptStatus);

    Boolean existsByUserIdAndIsReadFalseAndDeletedAtIsNull(Long userId);
}
