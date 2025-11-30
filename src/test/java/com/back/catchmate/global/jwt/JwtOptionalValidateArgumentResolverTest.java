package com.back.catchmate.global.jwt;

import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtOptionalValidateArgumentResolverTest {

    @InjectMocks
    private JwtOptionalValidateArgumentResolver resolver;

    @Mock
    private JwtService jwtService;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private MethodParameter methodParameter;

    private HttpServletRequest mockRequest;
    private static final String ACCESS_TOKEN_HEADER = "AccessToken";
    private static final String VALID_TOKEN = "valid_jwt";
    private static final Long USER_ID = 10L;

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
        // [수정] lenient() 추가: supportsParameter 테스트에서 호출되지 않아도 예외가 발생하지 않도록 함
        lenient().when(webRequest.getNativeRequest()).thenReturn(mockRequest);
    }

    // --- 1. supportsParameter 테스트 ---

    @Test
    @DisplayName("supportsParameter - @OptionalJwtValidation과 Long 타입이 모두 만족하면 True를 반환")
    void supportsParameter_Success() {
        // given
        when(methodParameter.hasParameterAnnotation(OptionalJwtValidation.class)).thenReturn(true);

        // [수정] doReturn().when() 구문 사용: Class<Long> 반환 오류 해결
        doReturn(Long.class).when(methodParameter).getParameterType();

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("supportsParameter - 어노테이션이 없으면 False를 반환")
    void supportsParameter_Fail_NoAnnotation() {
        // given
        when(methodParameter.hasParameterAnnotation(OptionalJwtValidation.class)).thenReturn(false);

        // [수정] Mocking된 메서드가 호출되지 않아도 되지만, 안전을 위해 doReturn() 유지
        doReturn(Long.class).when(methodParameter).getParameterType();

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isFalse();
    }

    // --- 2. resolveArgument 테스트 ---

    @Test
    @DisplayName("resolveArgument - 토큰이 존재하면 JwtService를 통해 userId를 반환한다")
    void resolveArgument_Success_TokenPresent() throws Exception {
        // given
        when(mockRequest.getHeader(ACCESS_TOKEN_HEADER)).thenReturn(VALID_TOKEN);
        when(jwtService.parseJwtToken(VALID_TOKEN)).thenReturn(USER_ID);

        // when
        Object result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        // then
        assertThat(result).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("resolveArgument - 토큰이 없거나 null이면 null을 반환한다 (Guest 허용)")
    void resolveArgument_Success_TokenMissing() throws Exception {
        // given
        when(mockRequest.getHeader(ACCESS_TOKEN_HEADER)).thenReturn(null); // 토큰 헤더 없음

        // when
        Object result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        // then
        assertThat(result).isNull();
        verify(jwtService, never()).parseJwtToken(anyString()); // JwtService가 호출되면 안 됨
    }

    @Test
    @DisplayName("resolveArgument - 토큰은 있지만 JwtService에서 예외 발생 시 전파한다")
    void resolveArgument_Fail_InvalidToken() {
        // given
        when(mockRequest.getHeader(ACCESS_TOKEN_HEADER)).thenReturn(VALID_TOKEN);
        // JwtService가 토큰 에러를 던진다고 가정
        when(jwtService.parseJwtToken(VALID_TOKEN)).thenThrow(new BaseException(ErrorCode.INVALID_TOKEN));

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(BaseException.class)
                .extracting("message")
                .isEqualTo(ErrorCode.INVALID_TOKEN.getMessage());
    }
}
