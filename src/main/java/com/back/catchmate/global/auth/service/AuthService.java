package com.back.catchmate.global.auth.service;

import com.back.catchmate.global.auth.dto.request.AuthRequest;
import com.back.catchmate.global.auth.dto.response.AuthResponse.LoginInfo;
import com.back.catchmate.global.auth.dto.response.AuthResponse.NicknameCheckInfo;
import com.back.catchmate.global.auth.dto.response.AuthResponse.ReissueInfo;
import com.back.catchmate.global.dto.StateResponse;

public interface AuthService {
    LoginInfo login(AuthRequest.LoginRequest loginRequest);

    NicknameCheckInfo checkNickname(String nickName);

    ReissueInfo reissue(String refreshToken);

    StateResponse logout(String refreshToken);
}
