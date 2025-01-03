package com.back.catchmate.global.auth.converter;

import com.back.catchmate.global.auth.dto.AuthResponse.AuthInfo;
import org.springframework.stereotype.Component;

@Component
public class AuthConverter {

    public AuthInfo toLoginInfo(String accessToken, String refreshToken, Boolean isFirstLogin) {
        return AuthInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isFirstLogin(isFirstLogin)
                .build();
    }
}
