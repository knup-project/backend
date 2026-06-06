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
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다."),

    // ── Session ───────────────────────────────────────────────────
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다."),
    SESSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "세션에 대한 권한이 없습니다."),
    NOT_TEAM_MODE(HttpStatus.BAD_REQUEST, "팀 모드 세션이 아닙니다."),
    INVALID_PIN(HttpStatus.NOT_FOUND, "해당 PIN의 세션을 찾을 수 없습니다."),
    NICKNAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    SESSION_FULL(HttpStatus.BAD_REQUEST, "세션 정원이 가득 찼습니다."),
    SESSION_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "이미 시작되었거나 종료된 세션입니다."),
    SESSION_NOT_STARTED(HttpStatus.BAD_REQUEST, "아직 시작되지 않은 세션입니다."),
    NO_MORE_QUESTIONS(HttpStatus.BAD_REQUEST, "더 이상 진행할 문제가 없습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "참가자를 찾을 수 없습니다."),
    ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "이미 답안을 제출했습니다."),
    TIME_EXPIRED(HttpStatus.BAD_REQUEST, "응답 시간이 초과되었습니다."),

    // ── AI ────────────────────────────────────────────────────────
    AI_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "AI 서비스 호출에 실패했습니다."),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI 요청 한도를 초과했습니다."),

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
