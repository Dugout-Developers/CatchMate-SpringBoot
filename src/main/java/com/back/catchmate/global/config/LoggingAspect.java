package com.back.catchmate.global.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final HttpServletRequest request;

    @Pointcut("execution(* com.back.catchmate.domain..controller..*(..))")
    private void allControllerMethods() {}

    @Pointcut("execution(* com.back.catchmate.global.auth..*.*(..))")
    private void authControllerMethods() {}

    // Pointcut에 의해 필터링된 경로로 들어오는 경우 메서드 호출 전에 적용
    @Before("allControllerMethods() || authControllerMethods()")
    public void beforeParameterLog(JoinPoint joinPoint) {
        // 메서드 정보 받아오기
        Method method = getMethod(joinPoint);
        log.info("======= method name = {} =======", method.getName());

        // 파라미터 받아오기
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            log.info("no parameter");
        } else {
            for (Object arg : args) {
                if (arg != null) {
                    log.info("parameter type = {}", arg.getClass().getSimpleName());
                    log.info("parameter value = {}", arg);
                } else {
                    log.info("parameter value is null");
                }
            }
        }

//        // 헤더에서 AccessToken과 RefreshToken 값 추출 및 로그 출력
//        String accessToken = request.getHeader("AccessToken");
//        String refreshToken = request.getHeader("RefreshToken");
//
//        if (accessToken != null) {
//            log.info("AccessToken = {}", accessToken);
//        } else {
//            log.info("No AccessToken found");
//        }
//
//        if (refreshToken != null) {
//            log.info("RefreshToken = {}", refreshToken);
//        } else {
//            log.info("No RefreshToken found");
//        }
    }

    // Poincut에 의해 필터링된 경로로 들어오는 경우 메서드 리턴 후에 적용
    @AfterReturning(value = "allControllerMethods() || authControllerMethods()", returning = "returnObj")
    public void afterReturnLog(JoinPoint joinPoint, Object returnObj) {
        // 메서드 정보 받아오기
        Method method = getMethod(joinPoint);
        log.info("======= method name = {} =======", method.getName());

        if (returnObj != null) {
            log.info("return type = {}", returnObj.getClass().getSimpleName());
            log.info("return value = {}", returnObj);
        }
    }

    // JoinPoint로 메서드 정보 가져오기
    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
