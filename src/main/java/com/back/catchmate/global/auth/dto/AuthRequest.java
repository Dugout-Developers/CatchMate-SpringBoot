package com.back.catchmate.global.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class AuthRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotNull
        @Email
        private String email;
        @NotNull
        private String providerId;
        @NotNull
        private String provider;
        @NotNull
        private String picture;
        @NotNull
        private String fcmToken;
    }
}
