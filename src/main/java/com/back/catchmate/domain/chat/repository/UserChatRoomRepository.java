package com.back.catchmate.domain.chat.repository;

import com.back.catchmate.domain.chat.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    Optional<UserChatRoom> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);
}
