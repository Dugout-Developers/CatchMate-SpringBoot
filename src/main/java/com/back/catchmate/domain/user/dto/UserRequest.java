package com.back.catchmate.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public abstract class UserRequest {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileUpdateRequest {
        @NotBlank
        private String nickName;
        @NotNull
        private Long favoriteClubId;
        @NotBlank
        private String watchStyle;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserJoinRequest {
        @Email
        private String email;
        @NotBlank
        private String providerId;
        @NotBlank
        private String provider;
        @NotBlank
        private String profileImageUrl;
        @NotBlank
        private String fcmToken;
        @NotNull
        private Character gender;
        @NotNull
        @Size(min = 2, max = 10)
        private String nickName;
        @NotNull
        private LocalDate birthDate;
        @NotNull
        private Long favoriteClubId;
        private String watchStyle;
    }
}
