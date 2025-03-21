package com.back.catchmate.global.jwt;

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
public class JwtOptionalValidateArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtService jwtService;
    private static final String ACCESS_TOKEN_HEADER = "AccessToken";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(OptionalJwtValidation.class);
        boolean hasLongType = Long.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && hasLongType;
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String accessToken = request.getHeader(ACCESS_TOKEN_HEADER);

        if (accessToken == null) {
            return null;
        }

        return jwtService.parseJwtToken(accessToken);
    }
}
