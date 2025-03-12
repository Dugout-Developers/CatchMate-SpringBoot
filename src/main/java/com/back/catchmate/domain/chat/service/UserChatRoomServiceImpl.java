package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.converter.UserChatRoomConverter;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfoList;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserChatRoomServiceImpl implements UserChatRoomService {
    private final ChatRoomService chatRoomService;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserChatRoomConverter userChatRoomConverter;

    @Override
    @Transactional(readOnly = true)
    public UserInfoList getUserInfoList(Long userId, Long chatRoomId) {
        if (!userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, chatRoomId)) {
            throw new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND);
        }

        List<UserChatRoom> userChatRoomList = userChatRoomRepository.findByChatRoomIdAndDeletedAtIsNull(chatRoomId);
        return userChatRoomConverter.toUserInfoList(userChatRoomList);
    }

    @Transactional(readOnly = true)
    public Boolean hasUnreadChat(Long userId) {
        List<UserChatRoom> userChatRoomList = userChatRoomRepository.findByUserIdAndDeletedAtIsNull(userId);

        return userChatRoomList.stream()
                .anyMatch(userChatRoom -> chatRoomService.getUnreadMessageCount(userId, userChatRoom.getChatRoom().getId()) > 0);
    }
}
