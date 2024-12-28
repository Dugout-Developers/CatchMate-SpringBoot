package com.back.catchmate.domain.user.entity;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.user.dto.UserRequest.UserProfileUpdateRequest;
import com.back.catchmate.global.entity.BaseTimeEntity;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Board> boardList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Enroll> enrollList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private char gender;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String watchStyle;

    @Column(nullable = false)
    private String profileImageUrl;

    @Column(nullable = false)
    private char allAlarm;           // 전체 알림

    @Column(nullable = false)
    private char chatAlarm;          // 채팅 알림

    @Column(nullable = false)
    private char enrollAlarm;        // 직관 신청 알림

    @Column(nullable = false)
    private char eventAlarm;         // 이벤트 알림

    @Column(nullable = false)
    private String fcmToken;

    // 프로필 정보 수정 메서드
    public void updateProfile(UserProfileUpdateRequest request, Club favoriteClub) {
        this.nickName = request.getNickName();
        this.club = favoriteClub;
        this.watchStyle = request.getWatchStyle();
    }

    // 프로필 이미지 URL 설정 메서드
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateAlarmSetting(AlarmType alarmType, char isEnabled) {
        switch (alarmType) {
            case ALL -> {
                this.allAlarm = isEnabled;
                this.chatAlarm = isEnabled;
                this.enrollAlarm = isEnabled;
                this.eventAlarm = isEnabled;
            }
            case CHAT -> this.chatAlarm = isEnabled;
            case ENROLL -> this.enrollAlarm = isEnabled;
            case EVENT -> this.eventAlarm = isEnabled;
            default -> throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // ALL 알림 상태 동기화
        syncAllAlarmStatus();
    }

    private void syncAllAlarmStatus() {
        // 개별 알림들이 모두 켜져 있으면 ALL 알림도 'Y'로 설정
        if (this.chatAlarm == 'Y' && this.enrollAlarm == 'Y' && this.eventAlarm == 'Y') {
            this.allAlarm = 'Y';
        }
        // 개별 알림 중 하나라도 꺼져 있으면 ALL 알림을 'N'으로 설정
        else {
            this.allAlarm = 'N';
        }
    }

    public boolean isNewFcmToken(String fcmToken) {
        return !Objects.equals(this.getFcmToken(), fcmToken);
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void deleteFcmToken() {
        this.fcmToken = null;
    }
}
