package com.back.catchmate.global.auth.controller;

import com.back.catchmate.global.auth.dto.AuthRequest.LoginRequest;
import com.back.catchmate.global.auth.dto.AuthResponse.AuthInfo;
import com.back.catchmate.global.auth.dto.AuthResponse.NicknameCheckInfo;
import com.back.catchmate.global.auth.dto.AuthResponse.ReissueInfo;
import com.back.catchmate.global.auth.service.AuthServiceImpl;
import com.back.catchmate.global.dto.StateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[사용자] 인증 & 인가 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authService;

    @PostMapping("/login")
    @Operation(summary = "로그인 & 회원가입 API", description = "회원가입/로그인을 통해 토큰을 발급하는 API 입니다.")
    public AuthInfo login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인 API", description = "닉네임의 중복 여부를 확인하는 API 입니다.")
    public NicknameCheckInfo checkNickname(@RequestParam("nickName") String nickName) {
        return authService.checkNickname(nickName);
    }

    @PostMapping("/reissue")
    @Operation(summary = "엑세스 토큰 재발급 API", description = "엑세스 토큰을 재발급하는 API 입니다.")
    public ReissueInfo reissue(@RequestHeader("RefreshToken") String refreshToken) {
        return authService.reissue(refreshToken);
    }

    @Operation(summary = "로그아웃 API", description = "로그아웃을 통해 리프레시 토큰을 삭제하는 API 입니다.")
    @DeleteMapping("/logout")
    public StateResponse logout(@RequestHeader("RefreshToken") String refreshToken) {
        return authService.logout(refreshToken);
    }
}
