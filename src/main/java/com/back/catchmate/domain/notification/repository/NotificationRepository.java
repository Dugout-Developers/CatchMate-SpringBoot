package com.back.catchmate.domain.notification.repository;

import com.back.catchmate.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
