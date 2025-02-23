package com.back.catchmate.domain.admin.notice.repository;

import com.back.catchmate.domain.admin.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT n FROM Notice n " +
            "WHERE (:startDateTime IS NULL OR n.createdAt >= :startDateTime) " +
            "AND (:endDateTime IS NULL OR n.createdAt <= :endDateTime)")
    Page<Notice> findNoticesWithinDateRange(@Param("startDateTime") LocalDateTime startDateTime,
                                            @Param("endDateTime") LocalDateTime endDateTime,
                                            Pageable pageable);

}

