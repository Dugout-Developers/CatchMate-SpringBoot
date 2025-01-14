package com.back.catchmate.domain.notification.controller;

import com.back.catchmate.domain.notification.dto.NotificationResponse.NotificationInfo;
import com.back.catchmate.domain.notification.dto.NotificationResponse.PagedNotificationInfo;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/receive")
    @Operation(summary = "내가 받은 알림 목록 조회 API", description = "내가 받은 알림 목록을 조회하는 API 입니다.")
    public PagedNotificationInfo getNotificationList(@JwtValidation Long userId,
                                                     @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                     @Parameter(hidden = true) Pageable pageable) {
        return notificationService.getNotificationList(userId, pageable);
    }

    @GetMapping("/receive/{notificationId}")
    @Operation(summary = "내가 받은 알림 단일 조회 API", description = "내가 받은 알림을 단일 조회하는 API 입니다.")
    public NotificationInfo getNotification(@JwtValidation Long userId,
                                            @PathVariable Long notificationId) {
        return notificationService.getNotification(userId, notificationId);
    }

    @DeleteMapping("/receive/{notificationId}")
    @Operation(summary = "내가 받은 알림 삭제 API", description = "내가 받은 알림을 삭제하는 API 입니다.")
    public StateResponse deleteNotification(@JwtValidation Long userId,
                                            @PathVariable Long notificationId) {
        return notificationService.deleteNotification(userId, notificationId);
    }
}
