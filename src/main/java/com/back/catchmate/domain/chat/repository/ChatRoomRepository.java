package com.back.catchmate.domain.chat.repository;

import com.back.catchmate.domain.chat.entity.ChatRoom;
import kotlin.OptIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByBoardId(Long boardId);

    boolean existsByBoardId(Long boardId);
}
