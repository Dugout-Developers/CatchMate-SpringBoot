package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatRoomInfo;
import com.back.catchmate.global.dto.StateResponse;
import org.springframework.data.domain.Pageable;

public interface ChatRoomService {
    StateResponse leaveChatRoom(Long userId, Long chatRoomId);

    PagedChatRoomInfo getChatRoomList(Long userId, Pageable pageable);
}
