package com.back.catchmate.domain.chat.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.chat.converter.ChatRoomConverter;
import com.back.catchmate.domain.chat.dto.ChatResponse.ChatRoomInfo;
import com.back.catchmate.domain.chat.dto.ChatResponse.PagedChatRoomInfo;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.enroll.repository.EnrollRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.back.catchmate.domain.chat.dto.ChatRequest.ChatMessageRequest.MessageType;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatService chatService;
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final EnrollRepository enrollRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomConverter chatRoomConverter;

    @Override
    @Transactional(readOnly = true)
    public PagedChatRoomInfo getChatRoomList(Long userId, Pageable pageable) {
        Page<UserChatRoom> userChatRoomList = userChatRoomRepository.findAllByUserId(userId, pageable);

        List<ChatRoomInfo> chatRoomInfoList = userChatRoomList.stream()
                .map(userChatRoom -> {
                    ChatRoom chatRoom = userChatRoom.getChatRoom();
                    Board board = chatRoom.getBoard();
                    int unreadMessageCount = (int) getUnreadMessageCount(userId, chatRoom.getId());
                    System.out.println("unreadMessageCount = " + unreadMessageCount);
                    return chatRoomConverter.toChatRoomInfo(chatRoom, userChatRoom, board, unreadMessageCount);
                })
                .collect(Collectors.toList());

        return new PagedChatRoomInfo(chatRoomInfoList, userChatRoomList.getTotalPages(),
                userChatRoomList.getTotalElements(), userChatRoomList.isFirst(),
                userChatRoomList.isLast()
        );
    }

    @Override
//    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long userId, Long chatRoomId) {
        // 사용자의 마지막 읽은 시간 조회
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));

        LocalDateTime lastReadTime = userChatRoom.getLastReadTime();

        // 특정 시간 이후의 메시지 개수 조회
        return chatMessageRepository.countByChatRoomIdAndSendTimeGreaterThanAndMessageType(chatRoomId, lastReadTime, "TALK");
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoomInfo getChatRoom(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndDeletedAtIsNull(chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));

        if (!userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, chatRoomId)) {
            throw new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND);
        }

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND));

        int unreadMessageCount = (int) getUnreadMessageCount(userId, chatRoomId);
        return chatRoomConverter.toChatRoomInfo(chatRoom, userChatRoom, chatRoom.getBoard(), unreadMessageCount);
    }

    @Override
    @Transactional
    public StateResponse leaveChatRoom(Long userId, Long chatRoomId) {
        // 채팅방과 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findByIdAndDeletedAtIsNull(chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));

        // UserChatRoom 엔티티를 찾아서 나가기
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(user.getId(), chatRoom.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND));

        if (chatRoom.isOwner(userId)) {
            userChatRoom.delete();
            chatRoom.getBoard().deleteBoard();
        } else {
            // 채팅방에서 나가기 처리
            userChatRoom.delete();
            // 게시글 현재 인원 수 감소
            chatRoom.getBoard().decrementCurrentPerson();
            // 채팅방에서 참여자 수 감소
            chatRoom.decrementParticipantCount();

            // 신청도 삭제 처리
            Enroll enroll = enrollRepository.findFirstByUserIdAndBoardIdAndDeletedAtIsNullAndAcceptStatusIs(userId, chatRoom.getBoard().getId(), AcceptStatus.ACCEPTED)
                    .orElseThrow(() -> new BaseException(ErrorCode.ENROLL_NOT_FOUND));

            enroll.delete();

            // 퇴장 메시지 보내기
            String content = user.getNickName() + " 님이 채팅을 떠났어요";  // 퇴장 메시지 내용
            chatService.sendEnterLeaveMessage(chatRoom.getId(), content, user.getId(), MessageType.LEAVE);
        }

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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, chatRoom.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND));

        // 채팅방에서 나가기 처리
        userChatRoom.delete();
        // 게시글 현재 인원 수 감소
        chatRoom.getBoard().decrementCurrentPerson();
        // 채팅방에서 참여자 수 감소
        chatRoom.decrementParticipantCount();

        String content = "방장의 결정으로 " + user.getNickName() + " 님이 채팅방에서 나갔습니다.";
        chatService.sendEnterLeaveMessage(chatRoomId, content, userId, MessageType.LEAVE);
        return new StateResponse(true);
    }

    @Override
    @Transactional
    public StateResponse updateNotificationSetting(Long userId, Long chatRoomId, boolean enable) {
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, chatRoomId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_CHATROOM_NOT_FOUND));

        userChatRoom.updateIsNotificationEnabled(enable);
        return new StateResponse(true);
    }
}
