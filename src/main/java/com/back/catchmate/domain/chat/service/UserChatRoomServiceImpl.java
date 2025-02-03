package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.converter.UserChatRoomConverter;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserChatRoomServiceImpl implements UserChatRoomService {
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserChatRoomConverter userChatRoomConverter;

    @Override
    @Transactional(readOnly = true)
    public UserResponse.UserInfoList getUserInfoList(Long chatRoomId) {
        List<UserChatRoom> userChatRoomList = userChatRoomRepository.findByChatRoomId(chatRoomId);
        return userChatRoomConverter.toUserInfoList(userChatRoomList);
    }
}
