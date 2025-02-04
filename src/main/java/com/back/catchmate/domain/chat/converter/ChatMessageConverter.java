package com.back.catchmate.domain.chat.converter;

import com.back.catchmate.domain.chat.dto.ChatResponse;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMessageConverter {
    public ChatResponse.PagedChatMessageInfo toPagedChatMessageInfo(Page<ChatMessage> chatMessageList) {
        List<ChatResponse.ChatMessageInfo> chatMessageInfoList = chatMessageList.stream()
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

    private ChatResponse.ChatMessageInfo toChatMessageInfo(ChatMessage chatMessage) {
        return ChatResponse.ChatMessageInfo.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getRoomId())
                .content(chatMessage.getContent())
                .senderId(chatMessage.getSenderId())
                .build();
    }

    public ChatMessage toChatMessage(Long chatRoomId, String content, Long senderId) {
        return ChatMessage.builder()
                .roomId(chatRoomId)
                .content(content)
                .senderId(senderId)
                .build();
    }
}
