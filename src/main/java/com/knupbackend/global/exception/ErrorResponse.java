package com.knupbackend.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String code;
    private String message;
    private String path;

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, "");
    }

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .path(path)
                .build();
    }

    public static ErrorResponse of(int status, String code, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status)
                .code(code)
                .message(message)
                .path(path)
                .build();
    }
}
