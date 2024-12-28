package com.back.catchmate.global.error.exception.serverError;

import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;

public class InternalServerException extends BaseException {
    public InternalServerException(ErrorCode errorCode) {
        super(errorCode);
    }
}
