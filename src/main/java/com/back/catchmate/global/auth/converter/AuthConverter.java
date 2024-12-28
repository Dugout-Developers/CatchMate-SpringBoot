package com.back.catchmate.global.auth.converter;

import com.back.catchmate.global.auth.dto.response.AuthResponse.LoginInfo;
import org.springframework.stereotype.Component;

@Component
public class AuthConverter {

    public LoginInfo toLoginInfo(String accessToken, String refreshToken, Boolean isFirstLogin) {
        return LoginInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isFirstLogin(isFirstLogin)
                .build();
    }
}
