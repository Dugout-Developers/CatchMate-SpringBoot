package com.back.catchmate.domain.chat.converter;

import com.back.catchmate.domain.chat.dto.ChatResponse;
import com.back.catchmate.domain.chat.dto.ChatResponse.ChatMessageInfo;
import com.back.catchmate.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMessageConverter {
    public Mono<ChatResponse.PagedChatMessageInfo> toPagedMessageInfo(Flux<ChatMessage> chatMessageFlux, Pageable pageable) {
        return chatMessageFlux.collectList()
                .flatMap(chatMessages -> {
                    // 전체 메시지 수를 구하고 페이지 계산
                    int totalElements = chatMessages.size();
                    int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
                    boolean isFirst = pageable.getPageNumber() == 0;
                    boolean isLast = pageable.getPageNumber() == totalPages - 1;

                    // ChatMessageInfo 리스트로 변환
                    List<ChatResponse.ChatMessageInfo> chatMessageInfoList = chatMessages.stream()
                            .map(this::toChatMessageInfo)
                            .collect(Collectors.toList());

                    // PagedChatMessageInfo 반환
                    return Mono.just(ChatResponse.PagedChatMessageInfo.builder()
                            .chatMessageInfoList(chatMessageInfoList)
                            .totalPages(totalPages)
                            .totalElements((long) totalElements)
                            .isFirst(isFirst)
                            .isLast(isLast)
                            .build());
                });
    }


    private ChatMessageInfo toChatMessageInfo(ChatMessage chatMessage) {
        return ChatMessageInfo.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getRoomId())
                .content(chatMessage.getContent())
                .senderId(chatMessage.getSenderId())
                .build();
    }
}
