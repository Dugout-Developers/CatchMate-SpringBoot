package com.back.catchmate.domain.user.entity;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private String favoriteClub;

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
}
