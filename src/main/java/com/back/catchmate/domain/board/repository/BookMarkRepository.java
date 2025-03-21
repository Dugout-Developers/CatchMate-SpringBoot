package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.entity.BookMark;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    boolean existsByUserAndBoardAndDeletedAtIsNull(User user, Board board);

    Optional<BookMark> findByUserIdAndBoardIdAndDeletedAtIsNull(Long userId, Long boardId);

    Page<BookMark> findAllByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    boolean existsByUserIdAndBoardIdAndDeletedAtIsNull(Long userId, Long boardId);
}
