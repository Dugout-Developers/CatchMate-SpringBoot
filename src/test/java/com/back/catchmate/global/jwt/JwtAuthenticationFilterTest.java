package com.back.catchmate.global.jwt;

import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.user.entity.Authority;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        // 테스트 간 SecurityContext 오염 방지
        SecurityContextHolder.clearContext();

        user = User.builder()
                .id(1L)
                .email("user@test.com")
                .authority(Authority.ROLE_USER)
                .build();

        admin = User.builder()
                .id(2L)
                .email("admin@test.com")
                .authority(Authority.ROLE_ADMIN)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰이 있으면 인증 객체를 생성하고 다음 필터로 진행한다")
    void doFilterInternal_Success() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("AccessToken", "valid_token");
        request.setRequestURI("/users/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtService.parseJwtToken("valid_token")).willReturn(user.getId());
        given(userRepository.findByIdAndDeletedAtIsNull(user.getId())).willReturn(Optional.of(user));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        // 1. SecurityContext에 인증 객체가 설정되었는지 확인 (상태 검증)
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user.getId());

        // 2. 다음 필터가 호출되었는지 확인 (행위 검증)
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("헤더에 토큰이 없으면 인증 없이 다음 필터로 진행한다 (익명 사용자)")
    void doFilterInternal_NoToken_Pass() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 헤더 없음
        request.setRequestURI("/public/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        // 1. Context는 비어있어야 함
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // 2. 다음 필터는 호출되어야 함 (인증 로직은 뒤의 SecurityFilter에서 처리)
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("관리자 페이지(/admin)에 일반 유저가 접근하면 403 Forbidden 에러를 응답한다")
    void doFilterInternal_Fail_Forbidden() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("AccessToken", "user_token");
        request.setRequestURI("/admin/dashboard"); // 관리자 경로
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtService.parseJwtToken("user_token")).willReturn(user.getId());
        given(userRepository.findByIdAndDeletedAtIsNull(user.getId())).willReturn(Optional.of(user));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        // 1. 상태 코드가 403인지 확인
        assertThat(response.getStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS.getHttpStatus().value());

        // 2. 응답 본문에 에러 메시지가 있는지 확인
        assertThat(response.getContentAsString()).contains(ErrorCode.FORBIDDEN_ACCESS.getMessage());

        // 3. 필터 체인이 중단되었는지 확인 (다음 필터 호출 X)
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("관리자 페이지(/admin)에 관리자가 접근하면 통과한다")
    void doFilterInternal_Success_Admin() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("AccessToken", "admin_token");
        request.setRequestURI("/admin/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtService.parseJwtToken("admin_token")).willReturn(admin.getId());
        given(userRepository.findByIdAndDeletedAtIsNull(admin.getId())).willReturn(Optional.of(admin));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰(파싱 실패)일 경우 401 Unauthorized 에러를 응답한다")
    void doFilterInternal_Fail_InvalidToken() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("AccessToken", "invalid_token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // JwtService에서 예외 발생 시뮬레이션
        given(jwtService.parseJwtToken("invalid_token")).willThrow(new RuntimeException("Parse Error"));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        // 1. 필터 내부의 catch 블록에서 INVALID_ACCESS_TOKEN(401)으로 응답해야 함
        assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_ACCESS_TOKEN.getHttpStatus().value());
        assertThat(response.getContentAsString()).contains(ErrorCode.INVALID_ACCESS_TOKEN.getMessage());

        // 2. Context는 비어있어야 함
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // 3. 필터 체인 중단
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰은 유효하지만 DB에 유저가 없는 경우 401 에러를 응답한다")
    void doFilterInternal_Fail_UserNotFound() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("AccessToken", "token_of_deleted_user");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtService.parseJwtToken(anyString())).willReturn(999L);
        // DB 조회 실패 (Optional.empty)
        given(userRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        // 코드상 예외(BaseException)가 발생하고 catch 블록에서 INVALID_ACCESS_TOKEN으로 매핑됨
        assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_ACCESS_TOKEN.getHttpStatus().value());

        verify(filterChain, never()).doFilter(request, response);
    }
}
