package com.back.catchmate.domain.enroll.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.converter.UserChatRoomConverter;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.enroll.converter.EnrollConverter;
import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.NewEnrollCountInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollRequestInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.UpdateEnrollInfo;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.enroll.repository.EnrollRepository;
import com.back.catchmate.domain.notification.service.FCMService;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static com.back.catchmate.domain.notification.message.NotificationMessages.*;

@Service
@RequiredArgsConstructor
public class EnrollServiceImpl implements EnrollService {
    private final FCMService fcmService;
    private final NotificationService notificationService;
    private final EnrollRepository enrollRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final EnrollConverter enrollConverter;
    private final UserChatRoomConverter userChatRoomConverter;

    @Override
    @Transactional
    public CreateEnrollInfo requestEnroll(CreateEnrollRequest request, Long boardId, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 존재하는 게시글인지, 자신의 게시글인지 확인
        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));
        if (board.isWriterSameAsLoginUser(user)) {
            throw new BaseException(ErrorCode.ENROLL_BAD_REQUEST);
        }

        enrollRepository.findByUserIdAndBoardIdAndDeletedAtIsNull(user.getId(), board.getId())
                .ifPresent(enroll -> {
                    throw new BaseException(ErrorCode.ENROLL_ALREADY_EXIST);
                });

        Enroll enroll = enrollConverter.toEntity(request, user, board);
        enrollRepository.save(enroll);

        String title = ENROLLMENT_NOTIFICATION_TITLE;
        String body = String.format(ENROLLMENT_NOTIFICATION_BODY, user.getNickName());

        // 게시글 작성자의 아이디를 통해 FCM 토큰 확인
        User boardWriter = userRepository.findById(board.getUser().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        // 게시글 작성자에게 푸시 알림 메세지 전송
        fcmService.sendMessage(boardWriter.getFcmToken(), title, body, boardId, AcceptStatus.PENDING);

        // 데이터베이스에 저장
        notificationService.createNotification(title, body, enroll.getUser().getProfileImageUrl(), boardId, boardWriter.getId(), AcceptStatus.PENDING);
        return enrollConverter.toCreateEnrollInfo(enroll);
    }

    @Override
    @Transactional
    public CancelEnrollInfo cancelEnroll(Long enrollId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Enroll enroll = enrollRepository.findByIdAndDeletedAtIsNull(enrollId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENROLL_NOT_FOUND));

        // 직관 신청한 사용자와 로그인한 사용자가 일치하는지 확인
        if (enroll.isDifferentFromLoginUser(user)) {
            throw new BaseException(ErrorCode.ENROLL_CANCEL_INVALID);
        }

        enroll.delete();
        return enrollConverter.toCancelEnrollInfo(enroll);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedEnrollRequestInfo getRequestEnrollList(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Enroll> enrollList = enrollRepository.findByUserIdAndDeletedAtIsNull(user.getId(), pageable);
        return enrollConverter.toPagedEnrollRequestInfo(enrollList);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedEnrollReceiveInfo getReceiveEnrollList(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Enroll> enrollList = enrollRepository.findEnrollListByBoardWriter(user.getId(), pageable);
        return enrollConverter.toPagedEnrollReceiveInfo(enrollList);
    }

    @Override
    @Transactional
    public PagedEnrollReceiveInfo getReceiveEnrollListByBoardId(Long userId, Long boardId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        // 게시글 작성자가 맞는지 확인
        if (!board.getUser().equals(user)) {
            throw new BaseException(ErrorCode.ENROLL_GET_INVALID);
        }

        // 게시글에 신청된 목록 조회
        Page<Enroll> enrollList = enrollRepository.findByBoardIdAndDeletedAtIsNull(boardId, pageable);

        if (enrollList.hasContent()) {
            List<Enroll> enrollsToUpdate = enrollList.getContent();
            enrollsToUpdate.forEach(enroll -> enroll.updateIsNew(false)); // 읽음 상태로 변경
        }

        return enrollConverter.toPagedEnrollReceiveInfo(enrollList);
    }

    @Override
    @Transactional(readOnly = true)
    public NewEnrollCountInfo getNewEnrollListCount(Long userId) {
        int enrollListCount = enrollRepository.countNewEnrollListByUserId(userId);
        return enrollConverter.toNewEnrollCountResponse(enrollListCount);
    }

    @Override
    @Transactional
    public UpdateEnrollInfo acceptEnroll(Long enrollId, Long userId) throws IOException {
        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Enroll enroll = enrollRepository.findById(enrollId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENROLL_NOT_FOUND));

        User boardWriter = enroll.getBoard().getUser();
        User enrollApplicant = userRepository.findById(enroll.getUser().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = enroll.getBoard();
        if (board.canIncrementCurrentPerson()) {
            board.incrementCurrentPerson();
        }

        // 게시글 작성자와 로그인한 사용자가 다를 경우 예외 발생
        if (loginUser.isDifferentUserFrom(boardWriter)) {
            throw new BaseException(ErrorCode.ENROLL_ACCEPT_INVALID);
        }

        enterChatRoom(loginUser, board);

        String title = ENROLLMENT_ACCEPT_TITLE;
        String body = ENROLLMENT_ACCEPT_BODY;

        // 직관 신청자에게 수락 푸시 알림 메세지 전송
        fcmService.sendMessage(enrollApplicant.getFcmToken(), title, body, enroll.getBoard().getId(), AcceptStatus.ACCEPTED);
        // 데이터베이스에 저장
        notificationService.createNotification(title, body, boardWriter.getProfileImageUrl(), enroll.getBoard().getId(), enrollApplicant.getId(), AcceptStatus.ACCEPTED);

        enroll.respondToEnroll(AcceptStatus.ACCEPTED);
        return enrollConverter.toUpdateEnrollInfo(enroll, AcceptStatus.ACCEPTED);
    }

    private void enterChatRoom(User loginUser, Board board) {
        ChatRoom chatRoom = chatRoomRepository.findByBoardId(board.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.CHATROOM_NOT_FOUND));

        UserChatRoom userChatRoom = userChatRoomConverter.toEntity(loginUser, chatRoom);
        userChatRoomRepository.save(userChatRoom);
    }

    @Override
    @Transactional
    public UpdateEnrollInfo rejectEnroll(Long enrollId, Long userId) throws IOException {
        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Enroll enroll = enrollRepository.findById(enrollId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENROLL_NOT_FOUND));

        User boardWriter = enroll.getBoard().getUser();
        User enrollApplicant = userRepository.findById(enroll.getUser().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 게시글 작성자와 로그인한 사용자가 다를 경우 예외 발생
        if (loginUser.isDifferentUserFrom(boardWriter)) {
            throw new BaseException(ErrorCode.ENROLL_REJECT_INVALID);
        }

        String title = ENROLLMENT_REJECT_TITLE;
        String body = ENROLLMENT_REJECT_BODY;

        // 직관 신청자에게 거절 푸시 알림 메세지 전송
        fcmService.sendMessage(enrollApplicant.getFcmToken(), title, body, enroll.getBoard().getId(), AcceptStatus.REJECTED);
        // 데이터베이스에 저장
        notificationService.createNotification(title, body, boardWriter.getProfileImageUrl(), enroll.getBoard().getId(), enrollApplicant.getId(), AcceptStatus.REJECTED);

        enroll.respondToEnroll(AcceptStatus.REJECTED);
        return enrollConverter.toUpdateEnrollInfo(enroll, AcceptStatus.REJECTED);
    }
}
