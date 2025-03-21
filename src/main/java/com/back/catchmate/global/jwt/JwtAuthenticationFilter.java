package com.back.catchmate.global.jwt;

import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.ErrorResponse;
import com.back.catchmate.global.error.exception.BaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // 요청 헤더에서 AccessToken을 가져오기 위한 상수
    private static final String ACCESS_TOKEN_HEADER = "AccessToken";
    // 응답 인코딩 타입을 설정하기 위한 상수
    private static final String ENCODING_TYPE = "UTF-8";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 요청 헤더에서 AccessToken을 가져옴
            String accessToken = request.getHeader(ACCESS_TOKEN_HEADER);
            if (accessToken != null && !accessToken.isEmpty()) {
                Long userId = jwtService.parseJwtToken(accessToken);

                // 사용자 조회 (userRepository에서 User 객체 가져오기)
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

                // 사용자 권한 확인
                if (!user.getAuthority().name().equals("ROLE_ADMIN") && request.getRequestURI().startsWith("/admin")) {
                    setErrorResponse(response, ErrorCode.FORBIDDEN_ACCESS);
                    return; // 필터 체인 실행 중단
                }

                // 사용자 권한 Security Context에 저장
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getAuthority().name()));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            setErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    // 에러 응답을 설정하는 메서드
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) {
        ObjectMapper objectMapper = new ObjectMapper();
        // 응답 상태 코드 설정
        response.setStatus(errorCode.getHttpStatus().value());
        // 응답 ContentType 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 응답 인코딩 설정
        response.setCharacterEncoding(ENCODING_TYPE);
        // 에러 응답 객체 생성
        ErrorResponse errorResponse = new ErrorResponse(errorCode.getHttpStatus(), errorCode.getMessage());
        try {
            // 에러 응답을 JSON 형식으로 변환하여 응답에 작성
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (IOException e) {
            // 예외 발생 시 스택 트레이스를 출력
            e.printStackTrace();
        }
    }
}
