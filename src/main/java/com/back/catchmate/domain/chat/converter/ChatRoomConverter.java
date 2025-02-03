package com.back.catchmate.domain.chat.converter;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.chat.dto.ChatResponse.ChatRoomInfo;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatRoomInfo;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatRoomConverter {
    private final BoardConverter boardConverter;

    public ChatRoom toEntity(Board board) {
        return ChatRoom.builder()
                .board(board)
                .participantCount(1)
                .lastMessageAt(LocalDateTime.now())
                .build();
    }

    public PagedChatRoomInfo toPagedChatRoomInfo(Page<UserChatRoom> userChatRoomList) {
        List<ChatRoomInfo> chatRoomInfoList = userChatRoomList.stream()
                .map(userChatRoom -> toChatRoomInfo(userChatRoom.getChatRoom(), userChatRoom.getChatRoom().getBoard()))
                .collect(Collectors.toList());

        return PagedChatRoomInfo.builder()
                .chatRoomInfoList(chatRoomInfoList)
                .totalPages(userChatRoomList.getTotalPages())
                .totalElements(userChatRoomList.getTotalElements())
                .isFirst(userChatRoomList.isFirst())
                .isLast(userChatRoomList.isLast())
                .build();
    }

    public ChatRoomInfo toChatRoomInfo(ChatRoom chatRoom, Board board) {
        BoardInfo boardInfo = boardConverter.toBoardInfo(board, board.getGame());

        return ChatRoomInfo.builder()
                .chatRoomId(chatRoom.getId())
                .boardInfo(boardInfo)
                .participantCount(chatRoom.getParticipantCount())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .build();
    }
}
