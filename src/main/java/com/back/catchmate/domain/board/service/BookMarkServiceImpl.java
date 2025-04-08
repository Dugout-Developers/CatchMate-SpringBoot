package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.converter.BookMarkConverter;
import com.back.catchmate.domain.board.dto.BoardResponse.PagedBoardInfo;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookMarkServiceImpl implements BookMarkService {

    private final BookMarkRepository bookMarkRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BookMarkConverter bookMarkConverter;
    private final BoardConverter boardConverter;

    @Override
    @Transactional
    public StateResponse addBookMark(Long userId, Long boardId) {
        User user = findUserById(userId);
        Board board = findBoardById(boardId);

        validateBookMarkAddition(user, board);

        BookMark bookMark = bookMarkConverter.toEntity(user, board);
        bookMarkRepository.save(bookMark);
        return new StateResponse(true);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedBoardInfo getBookMarkBoardList(Long userId, Pageable pageable) {
        User user = findUserById(userId);
        Page<BookMark> bookMarkPage = bookMarkRepository.findAllByUserIdAndDeletedAtIsNull(user.getId(), pageable);

        return boardConverter.toPagedBoardInfoFromBookMarkList(bookMarkPage);
    }

    @Override
    @Transactional
    public StateResponse removeBookMark(Long userId, Long boardId) {
        User user = findUserById(userId);
        Board board = findBoardById(boardId);

        BookMark bookMark = bookMarkRepository.findByUserIdAndBoardIdAndDeletedAtIsNull(user.getId(), board.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.BOOKMARK_NOT_FOUND));

        bookMark.delete();
        return new StateResponse(true);
    }

    private User findUserById(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }

    private Board findBoardById(Long boardId) {
        return boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));
    }

    private void validateBookMarkAddition(User user, Board board) {
        if (!user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(ErrorCode.BOOKMARK_BAD_REQUEST);
        }

        if (bookMarkRepository.existsByUserAndBoardAndDeletedAtIsNull(user, board)) {
            throw new BaseException(ErrorCode.ALREADY_BOOKMARK);
        }
    }
}

