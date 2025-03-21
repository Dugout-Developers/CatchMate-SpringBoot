package com.back.catchmate.domain.enroll.repository;

import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EnrollRepository extends JpaRepository<Enroll, Long> {
    Optional<Enroll> findByIdAndDeletedAtIsNull(Long enrollId);

    Optional<Enroll> findByUserIdAndBoardIdAndDeletedAtIsNull(Long userId, Long boardId);

    Optional<Enroll> findFirstByUserIdAndBoardIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Long boardId);

    Optional<Enroll> findByUserIdAndBoardIdAndDeletedAtIsNullAndAcceptStatusIsNot(Long userId, Long boardId, AcceptStatus acceptStatus);

    Optional<Enroll> findFirstByUserIdAndBoardIdAndDeletedAtIsNullAndAcceptStatusIs(Long userId, Long boardId, AcceptStatus acceptStatus);

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Enroll e WHERE e.id = :enrollId")
    Optional<Enroll> findByIdWithLock(@Param("enrollId") Long enrollId);
}
