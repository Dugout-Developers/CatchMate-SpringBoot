package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    // 채팅방에서 나가기
    @Transactional
    public StateResponse leaveChatRoom(Long userId, Long chatRoomId) {
        // 채팅방과 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));

        // UserChatRoom 엔티티를 찾아서 나가기
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(user.getId(), chatRoom.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND));

        // 채팅방에서 나가기 처리
//        userChatRoom.delete();

        // 채팅방에서 참여자 수 감소
        chatRoom.decrementParticipantCount();

        return new StateResponse(true);
    }
}
