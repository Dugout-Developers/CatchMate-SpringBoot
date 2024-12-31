package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Modifying
    @Query("UPDATE Board b SET b.deletedAt = CURRENT_TIMESTAMP WHERE b.user.id = :userId AND b.id = :boardId")
    int softDeleteByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);
}
