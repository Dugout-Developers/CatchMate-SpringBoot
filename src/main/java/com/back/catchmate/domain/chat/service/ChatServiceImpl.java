package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.converter.ChatMessageConverter;
import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatMessageInfo;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageConverter chatMessageConverter;

    // 메시지를 특정 채팅방으로 전송
    @Override
    @Transactional
    public void sendChatMessage(Long chatRoomId, ChatMessageRequest request) {
        String destination = "/topic/chat." + chatRoomId;

        if (request.getMessageType() == MessageType.TALK) {
            // 날짜 메시지가 필요한지 확인
            if (isNewDateMessageNeeded(chatRoomId, LocalDateTime.now())) {
                ChatMessage dateMessage = chatMessageConverter.toDateMessage(chatRoomId, LocalDateTime.now());
                messagingTemplate.convertAndSend(destination, dateMessage);
                chatMessageRepository.insert(dateMessage);
            }

            ChatMessage chatMessage = chatMessageConverter.toChatMessage(chatRoomId, request.getContent(), request.getSenderId(), MessageType.TALK);
            chatMessageRepository.insert(chatMessage);

            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));

            chatRoom.updateLastMessageTime();
        }

        log.info("Sending message to: {}", destination);
        messagingTemplate.convertAndSend(destination, request);
    }

    private boolean isNewDateMessageNeeded(Long chatRoomId, LocalDateTime newMessageTime) {
        LocalDate newDate = newMessageTime.toLocalDate();
        ChatMessage chatMessage = chatMessageRepository.findFirstByChatRoomIdOrderBySendTimeDesc(chatRoomId);

        if (chatMessage == null) {
            return true;
        } else {
            LocalDate localDate = chatMessage.getSendTime().toLocalDate();
            return !localDate.equals(newDate);
        }
    }

    @Override
    @Transactional
    public void sendEnterLeaveMessage(Long chatRoomId, String content, Long senderId, MessageType messageType) {
        // 메시지를 DB에 저장
        ChatMessage chatMessage = chatMessageConverter.toEnterLeaveMessage(chatRoomId, content, senderId, messageType);
        chatMessageRepository.save(chatMessage);

        // WebSocket을 통해 실시간 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat." + chatRoomId, chatMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedChatMessageInfo getChatMessageList(Long userId, Long chatRoomId, Pageable pageable) {
        if (!userChatRoomRepository.existsByUserIdAndChatRoomId(userId, chatRoomId)) {
            throw new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND);
        }

        Page<ChatMessage> chatMessageList = chatMessageRepository.findByChatRoomIdOrderByIdDesc(chatRoomId, pageable);
        return chatMessageConverter.toPagedChatMessageInfo(chatMessageList);
    }
}
