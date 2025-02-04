package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.converter.ChatMessageConverter;
import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatMessageInfo;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageConverter chatMessageConverter;

    // 메시지를 특정 채팅방으로 전송
    @Override
    @Transactional
    public void sendMessage(Long chatRoomId, ChatMessageRequest request) {
        if (request.getMessageType() == MessageType.TALK) {
            ChatMessage chatMessage = chatMessageConverter.toChatMessage(chatRoomId, request.getContent(), request.getSenderId());
            chatMessageRepository.insert(chatMessage);
        }

        String destination = "/topic/chat." + chatRoomId;
        log.info("Sending message to: {}", destination);
        messagingTemplate.convertAndSend(destination, request);
    }

    public PagedChatMessageInfo getChatMessageList(Long userId, Long chatRoomId, Pageable pageable) {
        if (!userChatRoomRepository.existsByUserIdAndChatRoomId(userId, chatRoomId)) {
            throw new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND);
        }

        Page<ChatMessage> chatMessageList = chatMessageRepository.findByRoomIdOrderByIdDesc(chatRoomId, pageable);
        return chatMessageConverter.toPagedChatMessageInfo(chatMessageList);
    }
}
