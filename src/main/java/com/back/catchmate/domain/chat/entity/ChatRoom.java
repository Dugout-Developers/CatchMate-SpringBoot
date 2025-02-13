package com.back.catchmate.domain.chat.entity;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "chat_rooms")
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 현재 채팅방에 참여중인 참가자 수
    @Column(nullable = false)
    private int participantCount;

    private LocalDateTime lastMessageAt;

    private String lastMessageContent;

    private String chatRoomImage;

    // 채팅방에 메시지가 있을 때 마지막 메시지 시간 업데이트
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    // 참여자 수 증가 메소드
    public void incrementParticipantCount() {
        this.participantCount++;
    }

    // 참여자 수 감소 메소드
    public void decrementParticipantCount() {
        this.participantCount--;
    }

    public boolean isOwner(Long userId) {
        return this.board.getUser().getId().equals(userId);
    }

    public void updateChatRoomImage(String chatRoomImage) {
        this.chatRoomImage = chatRoomImage;
    }

    public void updateLastMessageContent(String content) {
        this.lastMessageContent = content;
    }
}
