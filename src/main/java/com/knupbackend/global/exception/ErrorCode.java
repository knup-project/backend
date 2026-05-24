package com.knupbackend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ── Auth ──────────────────────────────────────────────────────
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // ── Quiz ──────────────────────────────────────────────────────
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다."),
    QUIZ_ACCESS_DENIED(HttpStatus.FORBIDDEN, "퀴즈에 대한 권한이 없습니다."),

    // ── Common ────────────────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message    = message;
    }
}
