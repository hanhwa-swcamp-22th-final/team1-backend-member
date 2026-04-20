package com.conk.member.common.exception;

/*
 * 전역 예외 처리기다.
 * 컨트롤러에서 발생한 예외를 공통 응답 형태로 바꿔서 내려준다.
 */

import com.conk.member.common.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMemberException(MemberException exception) {
        log.warn("[MemberException] code={} message={}", exception.getErrorCode().getCode(), exception.getMessage());
        Map<String, Object> body = createErrorBody(
                exception.getErrorCode().getCode(),
                exception.getMessage()
        );

        return ResponseEntity.status(exception.getErrorCode().getStatus())
                .body(ApiResponse.fail(exception.getMessage(), body));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBadCredentialsException(BadCredentialsException exception) {
        log.warn("[BadCredentials] {}", exception.getMessage());
        Map<String, Object> body = createErrorBody(ErrorCode.INVALID_CREDENTIALS.getCode(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(exception.getMessage(), body));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMissingRequestHeaderException(MissingRequestHeaderException exception) {
        Map<String, Object> body = createErrorBody(ErrorCode.BAD_REQUEST.getCode(), exception.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getMessage(), body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationException(MethodArgumentNotValidException exception) {
        String validationMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        Map<String, Object> body = createErrorBody(ErrorCode.BAD_REQUEST.getCode(), validationMessage);

        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getMessage(), body));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        Map<String, Object> body = createErrorBody(ErrorCode.INTERNAL_ERROR.getCode(), exception.getMessage());

        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getMessage(), body));
    }

    private Map<String, Object> createErrorBody(String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        return body;
    }
}
