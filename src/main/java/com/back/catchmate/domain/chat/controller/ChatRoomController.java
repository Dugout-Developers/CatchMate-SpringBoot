package com.back.catchmate.domain.chat.controller;

import com.back.catchmate.domain.chat.service.ChatRoomServiceImpl;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRoomController {
    private final ChatRoomServiceImpl chatRoomService;

    @DeleteMapping("/rooms/{chatRoomId}/users")
    public StateResponse leaveChatRoom(@JwtValidation Long userId,
                                       @PathVariable Long chatRoomId) {
        return chatRoomService.leaveChatRoom(userId, chatRoomId);
    }
}
