package com.back.catchmate.domain.chat.event;

import com.back.catchmate.domain.chat.dto.ChatRequest;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChatReadEventListener {

    private final UserChatRoomRepository userChatRoomRepository;

    @Autowired
    public ChatReadEventListener(UserChatRoomRepository userChatRoomRepository) {
        this.userChatRoomRepository = userChatRoomRepository;
    }

    @EventListener
    @Transactional
    public void handleChatReadEvent(ChatRequest.ReadChatMessageRequest event) {
        userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(event.getUserId(), event.getChatRoomId())
                .ifPresent(userChatRoom -> {
                    userChatRoom.updateLastReadTime();
                    userChatRoomRepository.flush();
                });
    }
}
