package com.back.catchmate.domain.notification.entity;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.inquiry.entity.Inquiry;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.global.entity.BaseTimeEntity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notifications")
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private boolean isRead;

    @Enumerated(EnumType.STRING)
    private AcceptStatus acceptStatus;

    // 알림 수신 여부 설정 메서드
    public void markAsRead() {
        this.isRead = true;
    }

    public boolean isNotRead() {
        return !this.isRead;
    }

    public void updateAcceptStatus(AcceptStatus acceptStatus) {
        this.acceptStatus = acceptStatus;
    }
}
