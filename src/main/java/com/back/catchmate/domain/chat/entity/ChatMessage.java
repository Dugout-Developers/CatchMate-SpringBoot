package com.back.catchmate.domain.chat.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chatting_content") // 실제 몽고 DB 컬렉션 이름
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private ObjectId id;
    private Long roomId;
    private String content;
    private Long senderId;
    private LocalDateTime sendTime;
    private String messageType;
}
