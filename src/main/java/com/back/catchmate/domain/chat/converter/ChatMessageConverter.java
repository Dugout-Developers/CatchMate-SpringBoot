package com.back.catchmate.domain.chat.converter;

import com.back.catchmate.domain.chat.dto.ChatResponse;
import com.back.catchmate.domain.chat.dto.ChatResponse.ChatMessageInfo;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;

@Component
public class ChatMessageConverter {
    public ChatResponse.PagedChatMessageInfo toPagedChatMessageInfo(Page<ChatMessage> chatMessageList) {
        List<ChatMessageInfo> chatMessageInfoList = chatMessageList.stream()
                .map(this::toChatMessageInfo)
                .toList();

        return ChatResponse.PagedChatMessageInfo.builder()
                .chatMessageInfoList(chatMessageInfoList)
                .totalPages(chatMessageList.getTotalPages())
                .totalElements(chatMessageList.getTotalElements())
                .isFirst(chatMessageList.isFirst())
                .isLast(chatMessageList.isLast())
                .build();
    }

    private ChatMessageInfo toChatMessageInfo(ChatMessage chatMessage) {
        return ChatMessageInfo.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getChatRoomId())
                .content(chatMessage.getContent())
                .senderId(chatMessage.getSenderId())
                .messageType(MessageType.valueOf(chatMessage.getMessageType()))
                .build();
    }

    public ChatMessage toChatMessage(Long chatRoomId, String content, Long senderId, MessageType messageType) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .content(content)
                .senderId(senderId)
                .sendTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .messageType(messageType.name())
                .build();
    }

    public ChatMessage toEnterLeaveMessage(Long chatRoomId, String content, Long userId, MessageType messageType) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .content(content)
                .senderId(userId)
                .sendTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))  // 서울 시간으로 시간 설정
                .messageType(messageType.name())  // 메시지 타입 설정 (ENTER 또는 LEAVE)
                .build();
    }

    public ChatMessage toDateMessage(Long chatRoomId, LocalDateTime localDateTime) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .content(formatDate(localDateTime))
                .senderId(-1L)
                .messageType(MessageType.DATE.name())
                .build();
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN);
        return dateTime.format(formatter);
    }
}
