package com.back.catchmate.global.auth.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Builder
@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 1209600000)
public class RefreshToken {
    @Id
    @NotNull
    private String refreshToken;
    @NotNull
    private Long userId;

    public static RefreshToken of(String refreshToken, Long userId) {
        return RefreshToken
                .builder()
                .refreshToken(refreshToken)
                .userId(userId)
                .build();
    }
}
