package com.back.catchmate.global.config;

import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.jwt.JwtAuthenticationFilter;
import com.back.catchmate.global.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final String[] whiteList = {"/auth/**", "/users/additional-info", "/actuator/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger/**", "/v3/api-docs/**", "/error/**", "/clubs/**", "/ws/**"};

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            web.ignoring().requestMatchers(whiteList);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 설정 Disable
        http
                .csrf(AbstractHttpConfigurer::disable);

        // From 로그인 방식 disable
        http
                .formLogin(AbstractHttpConfigurer::disable);

        // Http basic 인증 방식 disable
        http
                .httpBasic(AbstractHttpConfigurer::disable);

        http
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(whiteList).permitAll() // 화이트리스트에 있는 경로는 누구나 접근 가능
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 전용
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                );

        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
