package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.converter.BookMarkConverter;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.entity.BookMark;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.board.repository.BookMarkRepository;
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
public class BookMarkServiceImpl implements BookMarkService {
    private final BookMarkRepository bookMarkRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BookMarkConverter bookMarkConverter;

    @Override
    @Transactional
    public StateResponse addBookMark(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        if (bookMarkRepository.existsByUserAndBoard(user, board)) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND);
        }

        BookMark bookMark = bookMarkConverter.toEntity(user, board);
        bookMarkRepository.save(bookMark);
        return new StateResponse(true);
    }

    @Override
    @Transactional
    public StateResponse removeBookMark(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        BookMark bookMark = bookMarkRepository.findByUserIdAndBoardId(user.getId(), board.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.BOOKMARK_NOT_FOUND));

        bookMark.delete();
        return new StateResponse(true);
    }
}
