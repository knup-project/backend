package com.knupbackend.global.exception;

import lombok.Getter;

@Getter
public class KnupException extends RuntimeException {

    private final ErrorCode errorCode;

    public KnupException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
