package com.back.catchmate.domain.user.controller;

import com.back.catchmate.domain.chat.service.UserChatRoomService;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserRequest;
import com.back.catchmate.domain.user.dto.UserResponse.LoginInfo;
import com.back.catchmate.domain.user.dto.UserResponse.PagedUserInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UnreadStatusInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UpdateAlarmInfo;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.AlarmType;
import com.back.catchmate.domain.user.service.BlockedUserService;
import com.back.catchmate.domain.user.service.UserService;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "[유저] 유저 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final BlockedUserService blockedUserService;
    private final NotificationService notificationService;
    private final UserChatRoomService userChatRoomService;
    private final UserConverter userConverter;

    @PostMapping("/additional-info")
    @Operation(summary = "추가 정보 입력 API", description = "최초 로그인시, 추가 정보를 입력하는 API 입니다.")
    public LoginInfo addProfile(@Valid @RequestBody UserRequest.UserJoinRequest request) {
        return userService.joinUser(request);
    }

    @GetMapping("/profile")
    @Operation(summary = "나의 정보 조회 API", description = "마이페이지에서 나의 모든 정보를 조회하는 API 입니다.")
    public UserInfo getMyProfile(@JwtValidation Long userId) {
        return userService.getMyProfile(userId);
    }

    @GetMapping("/profile/{profileUserId}")
    @Operation(summary = "유저 정보 조회 API", description = "다른 유저의 정보를 조회하는 API 입니다.")
    public UserInfo getOtherUserProfile(@JwtValidation Long userId,
                                        @PathVariable Long profileUserId) {
        return userService.getOtherUserProfile(userId, profileUserId);
    }

    @PatchMapping("/profile")
    @Operation(summary = "나의 정보 수정 API", description = "마이페이지에서 나의 정보를 수정하는 API 입니다.")
    public StateResponse updateProfile(@JwtValidation Long userId,
                                       @RequestPart("request") @Valid UserRequest.UserProfileUpdateRequest request,
                                       @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        return userService.updateProfile(request, profileImage, userId);
    }

    @Operation(summary = "알림 설정", description = "유저의 알람 수신 여부를 변경하는 API 입니다.")
    @PatchMapping("/alarm")
    public UpdateAlarmInfo updateAlarm(@JwtValidation Long userId,
                                       @RequestParam("alarmType") AlarmType alarmType,
                                       @RequestParam("isEnabled") char isEnabled) {
        return userService.updateAlarm(userId, alarmType, isEnabled);
    }

    @PostMapping("/block/{blockedUserId}")
    @Operation(summary = "유저 차단 API", description = "특정 유저를 차단하는 API 입니다.")
    public StateResponse blockUser(@JwtValidation Long userId,
                                   @PathVariable Long blockedUserId) {
        return blockedUserService.blockUser(userId, blockedUserId);
    }

    @DeleteMapping("/block/{blockedUserId}")
    @Operation(summary = "유저 차단 해제 API", description = "차단한 유저를 해제하는 API 입니다.")
    public StateResponse unblockUser(@JwtValidation Long userId,
                                     @PathVariable Long blockedUserId) {
        return blockedUserService.unblockUser(userId, blockedUserId);
    }

    @GetMapping("/block")
    @Operation(summary = "차단한 유저 목록 API", description = "내가 차단한 유저 목록을 조회하는 API 입니다.")
    public PagedUserInfo getBlockedUserList(@JwtValidation Long userId,
                                         @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                         @Parameter(hidden = true) Pageable pageable) {
        return blockedUserService.getBlockedUserList(userId, pageable);
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "유저 탈퇴 API", description = "유저가 해당 서비스를 탈퇴하는 API 입니다.")
    public StateResponse deleteUser(@JwtValidation Long userId) {
        return userService.deleteUser(userId);
    }

    @GetMapping("/block")
    @Operation(summary = "차단한 유저 목록 API", description = "내가 차단한 유저 목록을 조회하는 API 입니다.")
    public PagedUserInfo getBlockedUsers(@JwtValidation Long userId,
                                         @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                         @Parameter(hidden = true) Pageable pageable) {
        return blockedUserService.getBlockedUserList(userId, pageable);
    }

    @GetMapping("/has-unread")
    @Operation(summary = "읽지 않은 채팅 & 알림 여부 조회 API", description = "읽지 않은 채팅 & 알림 여부 조회하는 API 입니다.")
    public UnreadStatusInfo hasUnreadMessagesOrNotifications(@JwtValidation Long userId) {
        return userService.hasUnreadMessagesOrNotifications(userId);
    }
}
