package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse;
import com.back.catchmate.domain.chat.dto.ChatResponse.MessageInfo;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.*;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    // 메시지를 특정 채팅방으로 전송
    @Transactional
    public void sendMessage(ChatMessageRequest request) {
        if (request.getMessageType() == MessageType.TALK) {
            ChatMessage chatMessage = new ChatMessage(
                    request.getChatRoomId(), request.getContent(), request.getSender()
            );
            // DB에 메시지 저장
            System.out.println("Saving chat message: " + chatMessage); // 디버깅
            chatMessageRepository.insert(chatMessage).block();
        }

        String destination = "/topic/chatRoom/" + request.getChatRoomId();
        System.out.println("Sending message to: " + destination); // 디버깅
        messagingTemplate.convertAndSend(destination, request);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<MessageInfo> findChatMessageList(Long roomId) {
        return chatMessageRepository.findAllByRoomId(roomId)
                .map(chatMessage -> ChatResponse.MessageInfo.builder()
                        .id(chatMessage.getId()) // MongoDB ObjectId
                        .roomId(chatMessage.getRoomId())
                        .content(chatMessage.getContent())
                        .sender(chatMessage.getSender())
                        .build()
                );
    }
}
