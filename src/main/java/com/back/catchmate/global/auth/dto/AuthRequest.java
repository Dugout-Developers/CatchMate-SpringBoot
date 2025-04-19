package com.back.catchmate.global.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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
        @Email
        private String email;
        @NotEmpty
        private String providerId;
        @NotEmpty
        private String provider;
        private String picture;
        @NotEmpty
        private String fcmToken;
    }
}
