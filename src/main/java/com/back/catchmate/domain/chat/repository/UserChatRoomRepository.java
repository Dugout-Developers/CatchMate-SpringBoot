package com.back.catchmate.domain.chat.repository;

import com.back.catchmate.domain.chat.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
}
