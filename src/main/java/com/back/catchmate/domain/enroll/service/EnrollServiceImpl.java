package com.back.catchmate.domain.enroll.service;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.enroll.converter.EnrollConverter;
import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.enroll.repository.EnrollRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EnrollServiceImpl implements EnrollService {
    private final EnrollRepository enrollRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final EnrollConverter enrollConverter;

    @Override
    public CreateEnrollInfo createEnroll(CreateEnrollRequest request, Long boardId, Long userId) throws IOException {
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
    public EnrollResponse.CancelEnrollInfo cancelEnroll(Long enrollId, Long userId) {
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
}
