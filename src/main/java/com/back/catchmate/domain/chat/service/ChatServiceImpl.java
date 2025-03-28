package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.converter.ChatMessageConverter;
import com.back.catchmate.domain.chat.dto.ChatRequest;
import com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest;
import com.back.catchmate.domain.chat.dto.ChatResponse;
import com.back.catchmate.domain.chat.dto.ChatResponse.LastChatMessageUpdateInfo;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatMessageInfo;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.notification.service.FCMService;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final FCMService fcmService;
    private final ChatSessionService chatSessionService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageConverter chatMessageConverter;
    private final UserRepository userRepository;

    // 메시지를 특정 채팅방으로 전송
    @Override
    @Transactional
    public void sendChatMessage(Long chatRoomId, ChatMessageRequest request) throws FirebaseMessagingException {
        User user = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        String destination = "/topic/chat." + chatRoomId;

        if (request.getMessageType() == MessageType.TALK) {
            // 날짜 메시지가 필요한지 확인
            if (isNewDateMessageNeeded(chatRoomId, LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
                ChatMessage dateMessage = chatMessageConverter.toDateMessage(chatRoomId, LocalDateTime.now());
                messagingTemplate.convertAndSend(destination, dateMessage);
                chatMessageRepository.insert(dateMessage);
            }

            // 채팅 메시지 저장 및 전송
            ChatMessage chatMessage = chatMessageConverter.toChatMessage(chatRoomId, request.getContent(), request.getSenderId(), MessageType.TALK);
            ChatMessage saveChatMessage = chatMessageRepository.insert(chatMessage);
            ChatResponse.ChatMessageInfo chatMessageInfo = chatMessageConverter.toChatMessageInfo(saveChatMessage);
            messagingTemplate.convertAndSend(destination, chatMessageInfo);

            // 채팅방 정보 업데이트
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));
            chatRoom.updateLastMessageContent(request.getContent());
            chatRoom.updateLastMessageTime();

            // 채팅방 목록 실시간 업데이트
            LastChatMessageUpdateInfo lastMessageUpdate = chatMessageConverter.toLastChatMessageUpdateRequest(chatRoomId, request.getContent(), LocalDateTime.now());
            messagingTemplate.convertAndSend("/topic/chatList", lastMessageUpdate);

            // 채팅방 참여자가 2명 이상일 경우 알림 전송 로직 수행
            if (chatRoom.getParticipantCount() > 1) {
                // 채팅방에 참여한 사용자 중 접속 중이지 않은 사용자 필터링
                List<String> targetTokens = chatRoom.getUserChatRoomList().stream()
                        .filter(userChatRoom -> !chatSessionService.isUserInChatRoom(chatRoomId, userChatRoom.getUser().getId())) // 접속 중이지 않은 사용자
                        .filter(UserChatRoom::isNotificationEnabled)
                        .map(userChatRoom -> userChatRoom.getUser().getFcmToken()) // FCM 토큰 추출
                        .filter(Objects::nonNull) // FCM 토큰이 있는 사용자만 포함
                        .toList();

                // FCM 알림 전송
                if (!targetTokens.isEmpty()) {
                    fcmService.sendMessagesByTokens(chatRoomId, chatRoom.getBoard().getTitle(), request.getContent(), user.getFcmToken());
                }
            }
        }

        log.info("Sending message to: {}", destination);
    }

    private boolean isNewDateMessageNeeded(Long chatRoomId, LocalDateTime newMessageTime) {
        // 메시지의 날짜 비교를 위한 LocalDateTime으로 변환
        LocalDate newDate = newMessageTime.toLocalDate();
        ChatMessage chatMessage = chatMessageRepository.findFirstByChatRoomIdOrderBySendTimeDesc(chatRoomId);

        if (chatMessage == null) {
            return true;
        } else {
            LocalDateTime sendTimeInSeoul = chatMessage.getSendTime();
            LocalDate localDate = sendTimeInSeoul.toLocalDate();
            return !localDate.equals(newDate);
        }
    }

    @Override
    @Transactional
    public void updateLastReadTime(ChatRequest.ReadChatMessageRequest request) {
        log.info("Updating last read time for user: {} in chatRoom: {}", request.getUserId(), request.getChatRoomId());
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(request.getUserId(), request.getChatRoomId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND));

        userChatRoom.updateLastReadTime();
        userChatRoomRepository.flush();
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
    public PagedChatMessageInfo getChatMessageList(Long userId, Long roomId, Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public PagedChatMessageInfo getChatMessageList(Long userId, Long chatRoomId, String lastMessageId, int size) {
        if (!userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, chatRoomId)) {
            throw new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND);
        }

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        userChatRoom.updateLastReadTime();

        Page<ChatMessage> chatMessageList;

        if (lastMessageId == null) {
            chatMessageList = chatMessageRepository.findByChatRoomIdOrderByIdDesc(chatRoomId, PageRequest.of(0, size, Sort.by(Sort.Order.desc("_id"))));
        } else {
            Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Order.desc("_id")));
            ObjectId lastObjectId = new ObjectId(lastMessageId);
            chatMessageList = chatMessageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(chatRoomId, lastObjectId, pageable);
        }

        boolean isLast = (chatMessageList.getContent().size() < size);
        String nextLastMessageId = (isLast || chatMessageList.isEmpty())
                ? null
                : chatMessageList.getContent().get(chatMessageList.getContent().size() - 1).getId().toString();

        return chatMessageConverter.toPagedChatMessageInfo(chatMessageList, nextLastMessageId);
    }
}
