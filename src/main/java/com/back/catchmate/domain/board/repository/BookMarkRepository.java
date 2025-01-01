package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.entity.BookMark;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    boolean existsByUserAndBoard(User user, Board board);

    Optional<BookMark> findByUserIdAndBoardId(Long userId, Long boardId);
}
