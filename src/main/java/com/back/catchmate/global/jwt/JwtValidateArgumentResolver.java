package com.back.catchmate.global.jwt;

import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@AllArgsConstructor
public class JwtValidateArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtService jwtService;

    private static final String ACCESS_TOKEN_HEADER = "AccessToken";

    // 메서드 파라미터가 @JwtValidation 어노테이션을 가지고 있고, 타입이 Long일 경우 true를 반환
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasJwtValidationAnnotation = parameter.hasParameterAnnotation(JwtValidation.class);
        boolean hasLongType = Long.class.isAssignableFrom(parameter.getParameterType());
        return hasJwtValidationAnnotation && hasLongType;
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // 요청에서 HttpServletRequest 객체를 가져옴
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        // 요청 헤더에서 AccessToken을 가져옴
        String accessToken = request.getHeader(ACCESS_TOKEN_HEADER);

        // AccessToken이 없으면 INVALID_ACCESS_TOKEN 예외를 던짐
        if (accessToken == null) {
            throw new BaseException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        // AccessToken을 파싱하여 사용자 ID를 가져옴
        Long userId = jwtService.parseJwtToken(accessToken);
        return userId;
    }
}
