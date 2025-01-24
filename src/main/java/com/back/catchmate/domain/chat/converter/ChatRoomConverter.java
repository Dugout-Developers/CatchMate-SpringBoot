package com.back.catchmate.domain.chat.converter;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChatRoomConverter {
    public ChatRoom toEntity(Board board) {
        return ChatRoom.builder()
                .board(board)
                .participantCount(1)
                .lastMessageAt(LocalDateTime.now())
                .build();
    }
}
