package com.back.catchmate.domain.chat.service;

import com.back.catchmate.global.dto.StateResponse;

public interface ChatRoomService {
    StateResponse leaveChatRoom(Long userId, Long chatRoomId);
}
