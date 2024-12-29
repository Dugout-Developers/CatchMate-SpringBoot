package com.back.catchmate.domain.enroll.repository;

import com.back.catchmate.domain.enroll.entity.Enroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollRepository extends JpaRepository<Enroll, Long> {
    Optional<Enroll> findByUserIdAndBoardId(Long userId, Long boardId);
}
