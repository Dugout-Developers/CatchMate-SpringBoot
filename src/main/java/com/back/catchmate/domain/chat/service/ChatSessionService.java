package com.back.catchmate.domain.chat.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class ChatSessionService {
    // 채팅방별 접속 사용자 목록 (chatRoomId -> 사용자 ID Set)
    private final ConcurrentHashMap<Long, Set<Long>> chatRoomSessionMap = new ConcurrentHashMap<>();

    // 사용자가 채팅방에 접속했을 때 호출
    public void userJoined(Long chatRoomId, Long userId) {
        chatRoomSessionMap
            .computeIfAbsent(chatRoomId, k -> new CopyOnWriteArraySet<>())
            .add(userId);
    }

    // 사용자가 채팅방에서 나갔을 때 호출
    public void userLeft(Long chatRoomId, Long userId) {
        chatRoomSessionMap.getOrDefault(chatRoomId, new CopyOnWriteArraySet<>()).remove(userId);
    }

    // 특정 사용자가 채팅방에 접속 중인지 확인
    public boolean isUserInChatRoom(Long chatRoomId, Long userId) {
        return chatRoomSessionMap.getOrDefault(chatRoomId, new CopyOnWriteArraySet<>()).contains(userId);
    }
}
