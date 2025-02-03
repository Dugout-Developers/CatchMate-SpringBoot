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
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageConverter chatMessageConverter;
    private final UserChatRoomRepository userChatRoomRepository;

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

    public Mono<PagedChatMessageInfo> getChatMessageList(Long userId, Long chatRoomId, Pageable pageable) {
        // 동기 방식으로 수정된 메서드 호출
        if (!userChatRoomRepository.existsByUserIdAndChatRoomId(userId, chatRoomId)) {
            throw new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND);
        }

        Flux<ChatMessage> chatMessageList = chatMessageRepository.findByRoomIdOrderByIdDesc(chatRoomId, pageable);
        return chatMessageConverter.toPagedMessageInfo(chatMessageList, pageable);
    }
}
