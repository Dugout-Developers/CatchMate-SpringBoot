package com.back.catchmate.global.error;

import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.error.exception.clientError.BadRequestException;
import com.back.catchmate.global.error.exception.serverError.DataNotFoundException;
import com.back.catchmate.global.error.exception.serverError.InternalServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 1. @Valid / @Validated 유효성 검사 실패 시 발생 (HTTP Status 400)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindException(BindingResult bindingResult) {
        // 모든 필드 에러 메시지를 콤마와 공백(", ")으로 깔끔하게 결합
        String reason = bindingResult.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " : " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("ValidationException - {}", reason);
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, reason);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 2. 파라미터 타입 불일치 (TypeMismatchException) 및 필수 파라미터 누락 (MissingServletRequestParameterException) 처리 (HTTP Status 400)
     */
    @ExceptionHandler({TypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> notValidParameterException(Exception e) {
        log.warn("Parameter Mismatch/Missing: {}", e.getMessage(), e);
        final ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST.getHttpStatus(), ErrorCode.BAD_REQUEST.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 3.1. 데이터 찾지 못함 예외 처리 (DataNotFoundException - HTTP Status 404)
     * BaseException보다 명시적으로 처리하여 우선순위를 높입니다.
     */
    @ExceptionHandler(DataNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException e) {
        log.warn("[{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);

        final ErrorResponse errorResponse = new ErrorResponse(e.getHttpStatus(), e.getMessage());
        // [핵심] DataNotFoundException은 NOT_FOUND (404)를 반환해야 합니다.
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    /**
     * 3.2. 서버 내부 예외 처리 (InternalServerException - HTTP Status 500)
     */
    @ExceptionHandler(InternalServerException.class)
    protected ResponseEntity<ErrorResponse> handleInternalServerException(InternalServerException e) {
        log.error("[{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);

        final ErrorResponse errorResponse = new ErrorResponse(e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    /**
     * 3.3. 일반 비즈니스 로직 예외 (BaseException 및 BadRequestException) 처리
     */
    @ExceptionHandler({BaseException.class, BadRequestException.class})
    protected ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        log.warn("[{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);

        final ErrorResponse errorResponse = new ErrorResponse(e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    /**
     * 4. 시스템에서 예상하지 못한 모든 Runtime Exception 처리 (HTTP Status 500)
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> runtimeException(Exception e) {
        // 서버 에러는 ERROR 레벨로 전체 스택 트레이스 로깅
        log.error("Unhandled Runtime Exception: {}", e.getMessage(), e);

        final ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
