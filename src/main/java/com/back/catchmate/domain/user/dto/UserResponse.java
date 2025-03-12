package com.back.catchmate.domain.user.dto;

import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfo;
import com.back.catchmate.domain.user.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public abstract class UserResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginInfo {
        private Long userId;
        private String accessToken;
        private String refreshToken;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String email;
        private String profileImageUrl;
        private char gender;
        private char allAlarm;           // 전체 알림
        private char chatAlarm;          // 채팅 알림
        private char enrollAlarm;        // 직관 신청 알림
        private char eventAlarm;         // 이벤트 알림
        private String nickName;
        private ClubInfo favoriteClub;
        private LocalDate birthDate;
        private String watchStyle;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoList {
        private List<UserInfo> userInfoList;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedUserInfo {
        private List<UserInfo> userInfoList;
        private Integer totalPages;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UpdateAlarmInfo {
        private Long userId;
        private AlarmType alarmType;  // 변경된 알림 유형
        private char isEnabled;    // 알림 활성화 여부
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UnreadStatusInfo {
        private boolean hasUnreadChat;
        private boolean hasUnreadNotification;
    }
}
