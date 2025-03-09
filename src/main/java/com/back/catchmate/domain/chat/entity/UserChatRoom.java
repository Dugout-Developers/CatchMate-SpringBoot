package com.back.catchmate.domain.chat.entity;

import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_chat_rooms")
public class UserChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_chat_room_id")
    private Long id;

    // 사용자와 채팅방 간의 다대다 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    // 채팅방에 참여한 시간
    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime lastReadTime;  // 마지막으로 채팅방을 읽은 시간

    private Boolean isNewChatRoom;  // 마지막으로 채팅방을 읽은 시간

    public void updateLastReadTime() {
        this.lastReadTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")); // KST(UTC+9) 기준으로 저장
        this.isNewChatRoom = false;  // 조회 시 새로운 채팅방이 아니게 변경
    }

}
