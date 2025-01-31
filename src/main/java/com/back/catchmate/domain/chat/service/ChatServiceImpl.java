package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse;
import com.back.catchmate.domain.chat.dto.ChatResponse.MessageInfo;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    // 메시지를 특정 채팅방으로 전송
    @Override
    @Transactional
    public void sendMessage(Long chatRoomId, ChatMessageRequest request) {
        if (request.getMessageType() == MessageType.TALK) {
            ChatMessage chatMessage = new ChatMessage(
                    chatRoomId, request.getContent(), request.getSenderId()
            );

            // DB에 메시지 저장
            chatMessageRepository.insert(chatMessage)
                    .doOnSuccess(savedMessage ->
                            log.info("Saving chat message: {}", chatMessage))
                    .subscribe();
        }

        String destination = "/topic/chat." + chatRoomId;
        log.info("Sending message to: {}", destination); // 디버깅
        messagingTemplate.convertAndSend(destination, request);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<MessageInfo> findChatMessageList(Long roomId) {
        return chatMessageRepository.findAllByRoomId(roomId)
                .map(chatMessage -> ChatResponse.MessageInfo.builder()
                        .id(chatMessage.getId())
                        .roomId(chatMessage.getRoomId())
                        .content(chatMessage.getContent())
                        .senderId(chatMessage.getSenderId())
                        .build()
                );
    }
}
