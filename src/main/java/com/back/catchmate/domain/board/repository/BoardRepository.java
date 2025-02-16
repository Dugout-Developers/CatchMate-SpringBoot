package com.back.catchmate.domain.board.repository;

import com.back.catchmate.domain.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom {
    @Query("SELECT b FROM Board b WHERE b.id = :boardId AND b.deletedAt IS NULL AND b.isCompleted = true")
    Optional<Board> findByIdAndDeletedAtIsNullAndIsCompleted(Long boardId);

    @Query("SELECT b FROM Board b WHERE b.id = :boardId AND b.deletedAt IS NULL")
    Optional<Board> findByIdAndDeletedAtIsNull(Long boardId);

    Page<Board> findAllByUserIdAndDeletedAtIsNullAndIsCompletedIsTrue(Long userId, Pageable pageable);

    Optional<Board> findTopByUserIdAndIsCompletedIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    long countByDeletedAtIsNullAndIsCompletedIsTrue();
}
