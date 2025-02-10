package com.back.catchmate.domain.chat.controller;

import com.back.catchmate.domain.chat.dto.ChatResponse;
import com.back.catchmate.domain.chat.service.ChatRoomService;
import com.back.catchmate.domain.chat.service.UserChatRoomService;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfoList;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "채팅방 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-rooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final UserChatRoomService userChatRoomService;

    @GetMapping("/list")
    @Operation(summary = "내가 속한 채팅방 조회 API", description = "내가 속해있는 채팅방을 조회하는 API 입니다.")
    public ChatResponse.PagedChatRoomInfo getChatRoomList(@JwtValidation Long userId,
                                                          @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                          @Parameter(hidden = true) Pageable pageable) {
        return chatRoomService.getChatRoomList(userId, pageable);
    }

    @DeleteMapping("/{chatRoomId}")
    @Operation(summary = "내가 속한 채팅방 나가기 API", description = "내가 속해있는 채팅방을 나가는 API 입니다.")
    public StateResponse leaveChatRoom(@JwtValidation Long userId,
                                       @PathVariable Long chatRoomId) {
        return chatRoomService.leaveChatRoom(userId, chatRoomId);
    }

    @GetMapping("/{chatRoomId}/user-list")
    @Operation(summary = "채팅방에 참여한 유저 정보 리스트 반환 API", description = "채팅방에 참여한 유저 정보 리스트 반환 API 입니다.")
    public UserInfoList getUsersInChatRoom(@PathVariable Long chatRoomId) {
        return userChatRoomService.getUserInfoList(chatRoomId);
    }

    @PatchMapping("/{chatRoomId}/image")
    @Operation(summary = "채팅방 이미지 변경 API", description = "채팅방 대표 이미지를 변경하는 API 입니다.")
    public StateResponse updateChatRoomImage(@JwtValidation Long userId,
                                             @PathVariable Long chatRoomId,
                                             @RequestParam("chatRoomImage") MultipartFile chatRoomImage) throws IOException {
        return chatRoomService.updateChatRoomImage(userId, chatRoomId, chatRoomImage);
    }

    @DeleteMapping("/{chatRoomId}/users/{userId}")
    @Operation(summary = "채팅방 유저 강제 퇴장 API", description = "채팅방에 참여한 유저를 강제 퇴장하는 API 입니다.")
    public StateResponse kickUserFromChatRoom(@JwtValidation Long loginUserId,
                                              @PathVariable Long chatRoomId,
                                              @PathVariable Long userId) {
        return chatRoomService.kickUserFromChatRoom(loginUserId, chatRoomId, userId);
    }
}
