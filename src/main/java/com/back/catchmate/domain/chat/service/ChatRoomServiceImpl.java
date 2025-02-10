package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.chat.converter.ChatRoomConverter;
import com.back.catchmate.domain.chat.dto.ChatResponse;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatService chatService;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatRoomConverter chatRoomConverter;

    @Override
    @Transactional(readOnly = true)
    public ChatResponse.PagedChatRoomInfo getChatRoomList(Long userId, Pageable pageable) {
        Page<UserChatRoom> userChatRoomList = userChatRoomRepository.findAllByUserId(userId, pageable);
        return chatRoomConverter.toPagedChatRoomInfo(userChatRoomList);
    }

    @Override
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
        userChatRoom.delete();

        // 게시글 현재 인원 수 감소
        chatRoom.getBoard().decrementCurrentPerson();
        // 채팅방에서 참여자 수 감소
        chatRoom.decrementParticipantCount();

        // 퇴장 메시지 보내기
        String content = user.getNickName() + " 님이 채팅을 떠났어요";  // 퇴장 메시지 내용
        chatService.sendEnterLeaveMessage(chatRoom.getId(), content, user.getId(), MessageType.LEAVE);

        return new StateResponse(true);
    }

    @Override
    @Transactional
    public StateResponse updateChatRoomImage(Long userId, Long chatRoomId, MultipartFile image) throws IOException {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));

        if (!chatRoom.isOwner(userId)) {
            throw new BaseException(ErrorCode.IMAGE_UPDATE_UNAUTHORIZED_ACCESS);
        }

        String imageUrl = s3Service.uploadFile(image);
        chatRoom.updateChatRoomImage(imageUrl);
        return new StateResponse(true);
    }

    @Override
    @Transactional
    public StateResponse kickUserFromChatRoom(Long loginUserId, Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));

        if (!chatRoom.isOwner(loginUserId)) {
            throw new BaseException(ErrorCode.KICK_CHATROOM_UNAUTHORIZED_ACCESS);
        }

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoom.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND));

        userChatRoom.delete();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        String content = "방장의 결정으로 " + user.getNickName() + " 님이 채팅방에서 나갔습니다.";
        chatService.sendEnterLeaveMessage(chatRoomId, content, userId, MessageType.LEAVE);
        return new StateResponse(true);
    }
}
