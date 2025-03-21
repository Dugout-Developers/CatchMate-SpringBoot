package com.back.catchmate.global.auth.service;

import com.back.catchmate.global.auth.dto.AuthRequest;
import com.back.catchmate.global.auth.dto.AuthResponse.AuthInfo;
import com.back.catchmate.global.auth.dto.AuthResponse.NicknameCheckInfo;
import com.back.catchmate.global.auth.dto.AuthResponse.ReissueInfo;
import com.back.catchmate.global.dto.StateResponse;

public interface AuthService {
    AuthInfo login(AuthRequest.LoginRequest loginRequest);

    NicknameCheckInfo checkNickname(String nickName);

    ReissueInfo reissue(String refreshToken);

    StateResponse logout(String refreshToken);
}
