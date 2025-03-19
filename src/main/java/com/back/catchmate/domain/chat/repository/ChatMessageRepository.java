package com.back.catchmate.domain.chat.repository;

import com.back.catchmate.domain.chat.entity.ChatMessage;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    Page<ChatMessage> findByChatRoomIdOrderByIdDesc(Long chatRoomId, Pageable pageable);

    Page<ChatMessage> findByChatRoomIdAndIdLessThanOrderByIdDesc(Long chatRoomId, ObjectId id, Pageable pageable);

    ChatMessage findFirstByChatRoomIdOrderBySendTimeDesc(Long roomId);

    long countByChatRoomIdAndSendTimeGreaterThanAndMessageType(Long chatRoomId, LocalDateTime lastReadTime, String messageType);

    void deleteAllByChatRoomId(Long chatRoomId);
}
