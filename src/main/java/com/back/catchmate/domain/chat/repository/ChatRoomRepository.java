package com.back.catchmate.domain.chat.repository;

import com.back.catchmate.domain.chat.entity.ChatRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByIdAndDeletedAtIsNull(Long chatRoomId);

    Optional<ChatRoom> findByBoardIdAndDeletedAtIsNull(Long boardId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChatRoom c WHERE c.board.id = :boardId AND c.deletedAt IS NULL")
    Optional<ChatRoom> findByBoardIdWithLock(@Param("boardId") Long boardId);

    boolean existsByBoardId(Long boardId);
}
