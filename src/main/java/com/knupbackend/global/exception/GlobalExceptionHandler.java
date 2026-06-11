package com.knupbackend.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(KnupException.class)
    public ResponseEntity<ErrorResponse> handleKnupException(KnupException e, HttpServletRequest request) {
        log.warn("KnupException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e,
                                                          HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, ErrorCode.INVALID_INPUT.name(), message, request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(HttpServletRequest request) {
        return ResponseEntity
                .status(ErrorCode.PDF_TOO_LARGE.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.PDF_TOO_LARGE, request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(org.springframework.web.HttpMediaTypeNotSupportedException e,
                                                         HttpServletRequest request) {
        log.warn("Unsupported media type: {} {}", request.getRequestURI(), e.getContentType());
        return ResponseEntity.status(415)
                .body(ErrorResponse.of(415, ErrorCode.INVALID_INPUT.name(),
                        "지원하지 않는 요청 형식입니다.", request.getRequestURI()));
    }

    /** 존재하지 않는 정적 경로(주로 봇 스캔) — ERROR 스택트레이스 없이 404만 응답 */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(HttpServletRequest request) {
        return ResponseEntity.status(404)
                .body(ErrorResponse.of(404, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected error", e);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI()));
    }
}
