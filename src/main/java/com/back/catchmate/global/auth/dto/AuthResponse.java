package com.back.catchmate.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class AuthResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthInfo {
        private String accessToken;
        private String refreshToken;
        private Boolean isFirstLogin;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class NicknameCheckInfo {
        private boolean isAvailable;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ReissueInfo {
        private String accessToken;
    }
}
