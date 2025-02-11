package com.back.catchmate.domain.topic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class TopicRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicSubscribeRequest {
        @NotNull
        private String topic;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicUnsubscribeRequest {
        @NotNull
        private String topic;
    }
}
