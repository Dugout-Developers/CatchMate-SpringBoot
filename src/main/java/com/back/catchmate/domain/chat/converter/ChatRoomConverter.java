package com.back.catchmate.domain.chat.converter;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.chat.dto.ChatResponse.ChatRoomInfo;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomConverter {
    private final BoardConverter boardConverter;

    public ChatRoom toEntity(Board board) {
        return ChatRoom.builder()
                .board(board)
                .participantCount(1)
                .lastMessageAt(null)
                .lastMessageContent(null)
                .chatRoomImage(String.valueOf(board.getClub().getId()))
                .build();
    }

    public ChatRoomInfo toChatRoomInfo(ChatRoom chatRoom, Board board, int unreadMessageCount) {
        BoardInfo boardInfo = boardConverter.toBoardInfo(board, board.getGame());

        return ChatRoomInfo.builder()
                .chatRoomId(chatRoom.getId())
                .boardInfo(boardInfo)
                .participantCount(chatRoom.getParticipantCount())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .lastMessageContent(chatRoom.getLastMessageContent())
                .chatRoomImage(chatRoom.getChatRoomImage())
                .unreadMessageCount(unreadMessageCount)
                .build();
    }
}
