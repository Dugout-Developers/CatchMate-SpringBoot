package com.back.catchmate.domain.enroll.repository;

import com.back.catchmate.domain.enroll.entity.Enroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EnrollRepository extends JpaRepository<Enroll, Long> {
    Optional<Enroll> findByIdAndDeletedAtIsNull(Long enrollId);

    Optional<Enroll> findByUserIdAndBoardIdAndDeletedAtIsNull(Long userId, Long boardId);

    Page<Enroll> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("""
            SELECT e FROM Enroll e
            JOIN e.board b
            WHERE b.user.id = :userId
            AND e.deletedAt IS NULL
            AND e.acceptStatus = 'PENDING'
            ORDER BY b.id DESC, e.createdAt DESC
            """)
    Page<Enroll> findEnrollListByBoardWriter(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT e FROM Enroll e WHERE e.board.id = :boardId AND e.deletedAt IS NULL AND e.acceptStatus = 'PENDING'")
    Page<Enroll> findByBoardIdAndDeletedAtIsNull(@Param("boardId") Long boardId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Enroll e JOIN e.board b WHERE e.isNew = true AND b.user.id = :userId AND e.deletedAt IS NULL")
    int countNewEnrollListByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndBoardIdAndDeletedAtIsNull(Long userId, Long boardId);
}
