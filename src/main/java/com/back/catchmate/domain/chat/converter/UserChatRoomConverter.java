package com.back.catchmate.domain.chat.converter;

import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfoList;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserChatRoomConverter {
    private final UserConverter userConverter;

    public UserChatRoom toEntity(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public UserInfoList toUserInfoList(List<UserChatRoom> userChatRoomList) {
        return new UserInfoList(userChatRoomList.stream()
                .map(userChatRoom -> userConverter.toUserInfo(userChatRoom.getUser()))
                .collect(Collectors.toList()));
    }
}
