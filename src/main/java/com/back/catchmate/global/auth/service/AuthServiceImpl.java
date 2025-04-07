package com.back.catchmate.global.auth.service;

import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.auth.converter.AuthConverter;
import com.back.catchmate.global.auth.dto.AuthRequest.LoginRequest;
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
@Transactional(readOnly = true)
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
    public AuthInfo login(LoginRequest loginRequest) {
        String providerIdWithProvider = generateProviderId(loginRequest);
        Optional<User> userOptional = userRepository.findByProviderIdAndDeletedAtIsNull(providerIdWithProvider);

        // 최초 로그인
        if (userOptional.isEmpty()) {
            return authConverter.toAuthInfo(null, null, true);
        }

        // 기존 사용자 로그인
        User user = userOptional.get();
        Long userId = user.getId();

        checkFcmToken(loginRequest, user);

        String accessToken = jwtService.createAccessToken(userId);
        String refreshToken = jwtService.createRefreshToken(userId);
        refreshTokenRepository.save(RefreshToken.of(refreshToken, userId));

        return authConverter.toAuthInfo(accessToken, refreshToken, false);
    }

    private String generateProviderId(LoginRequest request) {
        return request.getProviderId() + PROVIDER_ID_SEPARATOR + request.getProvider();
    }

    private void checkFcmToken(LoginRequest loginRequest, User user) {
        if (user.isNewFcmToken(loginRequest.getFcmToken())) {
            user.updateFcmToken(loginRequest.getFcmToken());
        }
    }

    // 닉네임 중복여부를 확인하는 메서드
    @Override
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
