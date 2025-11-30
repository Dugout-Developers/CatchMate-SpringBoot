package com.back.catchmate.global.auth.service;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.Provider;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private JwtService jwtService; // 실제 JWT 로직 사용

    // Redis Repository는 외부 의존성이므로 Mocking하여 테스트 격리
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    private User user;
    private Club club;

    @BeforeEach
    void setUp() {
        club = clubRepository.save(Club.builder()
                .name("KIA Tigers")
                .region("Gwangju")
                .homeStadium("Champions Field")
                .build());

        // 테스트용 유저 저장
        user = userRepository.save(User.builder()
                .email("test@test.com")
                .provider(Provider.GOOGLE)
                .providerId("12345@google") // providerId + separator + provider 형태가 아님에 주의 (DB 저장값)
                .gender('M')
                .nickName("TestUser")
                .birthDate(LocalDate.of(1990, 1, 1))
                .club(club)
                .profileImageUrl("default.jpg")
                .allAlarm('Y').chatAlarm('Y').enrollAlarm('Y').eventAlarm('Y')
                .fcmToken("old-fcm-token")
                .authority(Authority.ROLE_USER)
                .isReported(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        // 테스트 수행 후 생성된 데이터를 모두 삭제하여 DB 상태를 깨끗하게 비웁니다.
        userRepository.deleteAll();
        clubRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 - 최초 가입 유저는 isFirstLogin=true를 반환해야 한다")
    void login_FirstTime_Success() {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("new@test.com")
                .providerId("newId")
                .provider("kakao") // DB에 없는 조합
                .fcmToken("fcm-token")
                .build();

        // when
        AuthInfo authInfo = authService.login(request);

        // then
        assertThat(authInfo.getIsFirstLogin()).isTrue();
        assertThat(authInfo.getAccessToken()).isNull();
        assertThat(authInfo.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("로그인 - 기존 유저는 토큰을 발급하고 FCM 토큰을 업데이트해야 한다")
    void login_ExistingUser_Success() {
        // given
        // user.getProviderId()는 "google@12345"
        // generateProviderId 로직: request.providerId + "@" + request.provider
        // 따라서 request를 맞춰줘야 함
        LoginRequest request = LoginRequest.builder()
                .email(user.getEmail())
                .providerId("12345") // separator 앞부분
                .provider("google")  // separator 뒷부분
                .fcmToken("new-fcm-token") // 새로운 FCM 토큰
                .build();

        // when
        AuthInfo authInfo = authService.login(request);

        // then
        // 1. 응답 검증
        assertThat(authInfo.getIsFirstLogin()).isFalse();
        assertThat(authInfo.getAccessToken()).isNotNull();
        assertThat(authInfo.getRefreshToken()).isNotNull();

        // 2. DB 상태 검증 (FCM 토큰 업데이트 확인)
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getFcmToken()).isEqualTo("new-fcm-token");

        // 3. Redis 저장 검증
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 중복된 닉네임이면 false를 반환한다")
    void checkNickname_Duplicate() {
        // given
        String duplicateNickName = "TestUser"; // setUp에서 저장한 유저

        // when
        NicknameCheckInfo result = authService.checkNickname(duplicateNickName);

        // then
        assertThat(result.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 사용 가능한 닉네임이면 true를 반환한다")
    void checkNickname_Available() {
        // given
        String newNickName = "NewNickName";

        // when
        NicknameCheckInfo result = authService.checkNickname(newNickName);

        // then
        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("토큰 재발급 - 유효한 RefreshToken으로 AccessToken을 재발급한다")
    void reissue_Success() {
        // given
        // 실제 JwtService를 이용해 유효한 토큰 생성
        String validRefreshToken = jwtService.createRefreshToken(user.getId());

        // Mocking: Redis에 해당 토큰이 존재한다고 가정
        RefreshToken mockTokenEntity = RefreshToken.of(validRefreshToken, user.getId());
        given(refreshTokenRepository.findById(validRefreshToken)).willReturn(Optional.of(mockTokenEntity));

        // when
        ReissueInfo result = authService.reissue(validRefreshToken);

        // then
        assertThat(result.getAccessToken()).isNotNull();
        assertThat(result.getAccessToken()).startsWith("Bearer ");
    }

    @Test
    @DisplayName("토큰 재발급 - 저장소에 없는 RefreshToken이면 예외가 발생한다")
    void reissue_Fail_InvalidToken() {
        // given
        String validFormatToken = jwtService.createRefreshToken(user.getId());

        // Mocking: Redis에서 찾을 수 없음
        given(refreshTokenRepository.findById(validFormatToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(validFormatToken))
                .isInstanceOf(BaseException.class)
                // [수정] extracting("errorCode") 대신 hasMessage() 사용
                .hasMessage(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
    }

    @Test
    @DisplayName("로그아웃 - FCM 토큰 삭제 및 RefreshToken 삭제가 수행되어야 한다")
    void logout_Success() {
        // given
        String validRefreshToken = jwtService.createRefreshToken(user.getId());

        // when
        StateResponse response = authService.logout(validRefreshToken);

        // then
        assertThat(response.isState()).isTrue();

        // 1. DB 상태 검증 (FCM 토큰 삭제 확인)
        User loggedOutUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(loggedOutUser.getFcmToken()).isNull();

        // 2. Redis 삭제 검증
        verify(refreshTokenRepository).deleteById(validRefreshToken);
    }

    @Test
    @DisplayName("로그아웃 - 유효하지 않은 토큰(파싱 불가 등)이면 예외가 발생한다")
    void logout_Fail_InvalidToken() {
        // given
        String invalidToken = "invalid-token-string";

        // when & then
        assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(BaseException.class)
                // [수정] 여기도 동일하게 메시지로 검증
                .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
    }
}
