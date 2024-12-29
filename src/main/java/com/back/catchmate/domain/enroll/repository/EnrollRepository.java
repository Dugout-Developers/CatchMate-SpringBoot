package com.back.catchmate.domain.enroll.repository;

import com.back.catchmate.domain.enroll.entity.Enroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EnrollRepository extends JpaRepository<Enroll, Long> {
    Optional<Enroll> findByUserIdAndBoardId(Long userId, Long boardId);

    Page<Enroll> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Enroll e WHERE e.board.user.id = :userId")
    Page<Enroll> findEnrollListByBoardWriter(@Param("userId") Long userId, Pageable pageable);

    Page<Enroll> findByBoardId(Long boardId, Pageable pageable);
}
