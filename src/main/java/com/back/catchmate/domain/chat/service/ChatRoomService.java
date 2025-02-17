package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.dto.ChatResponse.ChatRoomInfo;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatRoomInfo;
import com.back.catchmate.global.dto.StateResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ChatRoomService {
    StateResponse leaveChatRoom(Long userId, Long chatRoomId);

    PagedChatRoomInfo getChatRoomList(Long userId, Pageable pageable);

    ChatRoomInfo getChatRoom(Long userId, Long chatRoomId);

    StateResponse updateChatRoomImage(Long userId, Long chatRoomId, MultipartFile image) throws IOException;

    StateResponse kickUserFromChatRoom(Long adminId, Long chatRoomId, Long userId);
}
