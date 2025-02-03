package com.back.catchmate.domain.chat.repository;

import com.back.catchmate.domain.chat.entity.UserChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    Optional<UserChatRoom> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    boolean existsByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    @Query("SELECT ucr FROM UserChatRoom ucr " +
            "JOIN FETCH ucr.chatRoom cr " +
            "WHERE ucr.user.id = :userId " +
            "ORDER BY cr.lastMessageAt DESC")
    Page<UserChatRoom> findAllByUserId(Long userId, Pageable pageable);
}
