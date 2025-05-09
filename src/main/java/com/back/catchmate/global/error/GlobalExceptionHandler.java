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


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindException(BindingResult bindingResult) {
        StringBuilder reason = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String errorMessage = fieldError.getField() + " : " + fieldError.getDefaultMessage();
            reason.append(errorMessage).append(", ");
        }
        log.warn("ValidationException - {}", reason);
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, String.valueOf(reason));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({TypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> notValidParameterException(Exception e) {
        log.warn("[{}] {} ({})", e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace()[0]);
        final ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST.getHttpStatus(), ErrorCode.BAD_REQUEST.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<ErrorResponse> badRequestException(BadRequestException e) {
        log.warn("[{}] {} ({})", e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace()[0]);
        final ErrorResponse errorResponse = new ErrorResponse(e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(InternalServerException.class)
    protected ResponseEntity<ErrorResponse> internalServerException(InternalServerException e) {
        e.printStackTrace();
        log.error("[{}] {} ({})", e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace()[0]);
        final ErrorResponse errorResponse = new ErrorResponse(e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        log.warn("[{}] {} ({})", e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace()[0]);
        final ErrorResponse errorResponse = new ErrorResponse(e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(DataNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNotDataFoundException(DataNotFoundException e) {
        log.warn("[{}] {} ({})", e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace()[0]);
        final ErrorResponse errorResponse = new ErrorResponse(e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> runtimeException(Exception e) {
        e.printStackTrace();
        log.error("[{}] {} ({})", e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace()[0]);
        final ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
