package com.back.catchmate.domain.enroll.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.enroll.converter.EnrollConverter;
import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollRequestInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.UpdateEnrollInfo;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.enroll.repository.EnrollRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollServiceImpl implements EnrollService {
    private final EnrollRepository enrollRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final EnrollConverter enrollConverter;

    @Override
    @Transactional
    public CreateEnrollInfo requestEnroll(CreateEnrollRequest request, Long boardId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 존재하는 게시글인지, 자신의 게시글인지 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));
        if (board.isWriterSameAsLoginUser(user)) {
            throw new BaseException(ErrorCode.ENROLL_BAD_REQUEST);
        }

        enrollRepository.findByUserIdAndBoardId(user.getId(), board.getId())
                .ifPresent(enroll -> {
                    throw new BaseException(ErrorCode.ENROLL_ALREADY_EXIST);
                });

        Enroll enroll = enrollConverter.toEntity(request, user, board);
        enrollRepository.save(enroll);
        return enrollConverter.toCreateEnrollInfo(enroll);
    }

    @Override
    @Transactional
    public CancelEnrollInfo cancelEnroll(Long enrollId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Enroll enroll = enrollRepository.findById(enrollId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENROLL_NOT_FOUND));

        // 직관 신청한 사용자와 로그인한 사용자가 일치하는지 확인
        if (enroll.isDifferentFromLoginUser(user)) {
            throw new BaseException(ErrorCode.ENROLL_CANCEL_INVALID);
        }

        enrollRepository.delete(enroll);
        return enrollConverter.toCancelEnrollInfo(enroll);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedEnrollRequestInfo getRequestEnrollList(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Page<Enroll> enrollList = enrollRepository.findByUserId(user.getId(), pageable);
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
    @Transactional(readOnly = true)
    public PagedEnrollReceiveInfo getReceiveEnrollListByBoardId(Long userId, Long boardId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        // 게시글 작성자가 맞는지 확인
        if (!board.getUser().equals(user)) {
            throw new BaseException(ErrorCode.ENROLL_GET_INVALID);
        }

        // 게시글에 신청된 목록 조회
        Page<Enroll> enrollList = enrollRepository.findByBoardId(boardId, pageable);
        return enrollConverter.toPagedEnrollReceiveInfo(enrollList);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollResponse.NewEnrollCountInfo getNewEnrollListCount(Long userId) {
        int enrollListCount = enrollRepository.countNewEnrollListByUserId(userId);
        return enrollConverter.toNewEnrollCountResponse(enrollListCount);
    }

    @Override
    @Transactional
    public UpdateEnrollInfo acceptEnroll(Long enrollId, Long userId) {
        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Enroll enroll = enrollRepository.findById(enrollId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENROLL_NOT_FOUND));

        User boardWriter = enroll.getBoard().getUser();
        User enrollApplicant = userRepository.findById(enroll.getUser().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 게시글 작성자와 로그인한 사용자가 다를 경우 예외 발생
        if (loginUser.isDifferentUserFrom(boardWriter)) {
            throw new BaseException(ErrorCode.ENROLL_ACCEPT_INVALID);
        }

        enroll.setAcceptStatus(AcceptStatus.ACCEPTED);
        return enrollConverter.toUpdateEnrollInfo(enroll, AcceptStatus.ACCEPTED);
    }

    @Override
    @Transactional
    public UpdateEnrollInfo rejectEnroll(Long enrollId, Long userId) {
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

        enroll.setAcceptStatus(AcceptStatus.REJECTED);
        return enrollConverter.toUpdateEnrollInfo(enroll, AcceptStatus.REJECTED);
    }
}
