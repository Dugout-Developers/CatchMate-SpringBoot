package com.back.catchmate.global.jwt;

import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.secretKey}")
    private String secretKey;
    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;
    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;
    @Value("${jwt.access.header}")
    private String ACCESS_TOKEN_SUBJECT;
    @Value("${jwt.refresh.header}")
    private String REFRESH_TOKEN_SUBJECT;

    // JWT 클레임에서 사용자 ID를 저장하는 키
    private static final String ID_CLAIM = "id";
    // Bearer 토큰 접두사
    private static final String BEARER = "Bearer ";

    // 액세스 토큰을 생성하는 메서드
    public String createAccessToken(Long userId) {
        String accessToken = createToken(userId, ACCESS_TOKEN_SUBJECT, accessTokenExpirationPeriod);
        return accessToken;
    }

    // 리프레시 토큰을 생성하는 메서드
    public String createRefreshToken(Long userId) {
        String refreshToken = createToken(userId, REFRESH_TOKEN_SUBJECT, refreshTokenExpirationPeriod);
        return refreshToken;
    }

    // 토큰을 생성하는 메서드
    private String createToken(Long userId, String tokenSubject, Long expirationPeriod) {
        Date expirationTime = new Date();
        // 토큰의 만료 시간 설정
        expirationTime.setTime(expirationTime.getTime() + expirationPeriod);

        Claims claims = Jwts.claims();
        // 클레임에 사용자 ID 추가
        claims.put(ID_CLAIM, userId);

        // JWT 빌더를 사용하여 토큰 생성
        String accessToken = Jwts.builder()
                .setSubject(tokenSubject)
                .setClaims(claims)
                .setExpiration(expirationTime)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secretKey.getBytes()))
                .compact();

        // Bearer 접두사를 붙여서 반환
        return BEARER + accessToken;
    }

    // JWT 토큰을 파싱하여 사용자 ID를 반환하는 메서드
    public Long parseJwtToken(String token) {
        try {
            // Bearer 접두사 제거
            token = BearerRemove(token);
            // 토큰을 파싱하여 클레임을 가져옴
            Claims claims = Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString(secretKey.getBytes()))
                    .parseClaimsJws(token)
                    .getBody();

            // 클레임에서 사용자 ID를 추출하여 반환
            return Long.valueOf((Integer) claims.get(ID_CLAIM));
        } catch (Exception e) {
            // 예외 발생 시 INVALID_TOKEN 에러를 던짐
            throw new BaseException(ErrorCode.INVALID_TOKEN);
        }
    }

    // Bearer 접두사를 제거하는 메서드
    private String BearerRemove(String token) {
        return token.substring(BEARER.length());
    }
}
