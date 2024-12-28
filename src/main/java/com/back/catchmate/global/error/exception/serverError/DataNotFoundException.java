package com.back.catchmate.global.error.exception.serverError;

import com.back.catchmate.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataNotFoundException extends RuntimeException {
    private final HttpStatus httpStatus;
    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getHttpStatus();
    }
}
