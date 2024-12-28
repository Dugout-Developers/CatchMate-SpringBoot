package com.back.catchmate.global.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class AuthResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginInfo {
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
