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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtValidateArgumentResolverTest {

    @InjectMocks
    private JwtValidateArgumentResolver resolver;

    @Mock
    private JwtService jwtService;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private MethodParameter methodParameter;

    private HttpServletRequest mockRequest;
    private static final String ACCESS_TOKEN_HEADER = "AccessToken";

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
        // resolveArgument 호출 시 NativeRequest를 반환하도록 기본 설정
        when(webRequest.getNativeRequest()).thenReturn(mockRequest);
    }

    @Test
    @DisplayName("resolveArgument - AccessToken이 null이면 INVALID_ACCESS_TOKEN 예외를 던진다")
    void resolveArgument_Fail_TokenMissing() {
        // given
        // HttpServletRequest의 getHeader("AccessToken")이 null을 반환하도록 설정
        when(mockRequest.getHeader(ACCESS_TOKEN_HEADER)).thenReturn(null); //

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(methodParameter, null, webRequest, null))
                .isInstanceOf(BaseException.class)
                .extracting("message")
                .isEqualTo(ErrorCode.INVALID_ACCESS_TOKEN.getMessage()); //
    }
}
