package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.user.dto.UserResponse.UserInfoList;

public interface UserChatRoomService {
    UserInfoList getUserInfoList(Long chatRoomId);
}
