package com.back.catchmate.global.error.exception.clientError;

import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;

public class BadRequestException extends BaseException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
