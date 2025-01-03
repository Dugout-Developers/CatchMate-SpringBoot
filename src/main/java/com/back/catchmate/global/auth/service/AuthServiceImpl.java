package com.back.catchmate.global.auth.service;

import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.auth.converter.AuthConverter;
import com.back.catchmate.global.auth.dto.AuthRequest;
import com.back.catchmate.global.auth.dto.AuthResponse.AuthInfo;
import com.back.catchmate.global.auth.dto.AuthResponse.NicknameCheckInfo;
import com.back.catchmate.global.auth.dto.AuthResponse.ReissueInfo;
import com.back.catchmate.global.auth.entity.RefreshToken;
import com.back.catchmate.global.auth.repository.RefreshTokenRepository;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthConverter authConverter;

    // Provider ID와 구분자를 결합하기 위한 상수
    public static final String PROVIDER_ID_SEPARATOR = "@";

    // 로그인 메서드
    @Override
    @Transactional
    public AuthInfo login(AuthRequest.LoginRequest loginRequest) {
        // Provider ID와 Provider를 결합한 문자열 생성
        String providerIdWithProvider = loginRequest.getProviderId() + PROVIDER_ID_SEPARATOR + loginRequest.getProvider();

        // 결합한 문자열로 사용자 조회
        Optional<User> findUserOptional = userRepository.findByProviderId(providerIdWithProvider);
        boolean isFirstLogin = false;
        AuthInfo authInfo;

        if (findUserOptional.isEmpty()) {
            // 사용자가 없으면 최초 회원가입 여부를 true 반환
            isFirstLogin = true;
            authInfo = authConverter.toLoginInfo(null, null, isFirstLogin);
        } else {
            // 회원가입된 사용자가 있으면 AccessToken과 RefreshToken 반환
            User user = findUserOptional.get();
            Long userId = user.getId();

            // FCM 토큰 변경 여부 확인
            checkFcmToken(loginRequest, user);

            // AccessToken과 RefreshToken을 생성
            String accessToken = jwtService.createAccessToken(userId);
            String refreshToken = jwtService.createRefreshToken(userId);

            // RefreshToken을 Redis에 저장
            refreshTokenRepository.save(RefreshToken.of(refreshToken, userId));
            authInfo = authConverter.toLoginInfo(accessToken, refreshToken, isFirstLogin);
        }

        return authInfo;
    }

    private void checkFcmToken(AuthRequest.LoginRequest loginRequest, User user) {
        if (user.isNewFcmToken(loginRequest.getFcmToken())) {
            user.updateFcmToken(loginRequest.getFcmToken());
        }
    }

    // 닉네임 중복여부를 확인하는 메서드
    @Override
    @Transactional(readOnly = true)
    public NicknameCheckInfo checkNickname(String nickName) {
        boolean isAvailable = !userRepository.existsByNickName(nickName);
        return new NicknameCheckInfo(isAvailable);
    }

    // 토큰 재발급 메서드
    @Override
    @Transactional
    public ReissueInfo reissue(String refreshToken) {
        // RefreshToken을 파싱하여 사용자 ID를 가져옴
        Long userId = jwtService.parseJwtToken(refreshToken);
        // RefreshToken이 유효한지 확인
        refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 새로운 AccessToken을 생성
        String accessToken = jwtService.createAccessToken(userId);
        return new ReissueInfo(accessToken);
    }

    // 로그아웃 메서드
    @Override
    @Transactional
    public StateResponse logout(String refreshToken) {
        // RefreshToken을 파싱하여 사용자 ID를 가져옴
        Long userId = jwtService.parseJwtToken(refreshToken);
        // 사용자 정보를 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // FCM 토큰 삭제
        user.deleteFcmToken();
        // RefreshToken을 Redis에서 삭제
        refreshTokenRepository.deleteById(refreshToken);
        return new StateResponse(true);
    }
}
