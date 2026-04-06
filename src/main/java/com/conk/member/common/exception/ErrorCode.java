package com.conk.member.common.exception;

/*
 * 전역 예외 처리에서 사용할 표준 오류 코드를 정의한다.
 */

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "M-400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "M-401", "인증에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "M-403", "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "M-404", "대상을 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "M-409", "충돌이 발생했습니다."),
    GONE(HttpStatus.GONE, "M-410", "이미 만료되었거나 사용할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "M-500", "서버 오류가 발생했습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-001", "이메일/작업자코드 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_ALREADY_USED(HttpStatus.CONFLICT, "AUTH-002", "이미 사용한 설정 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.GONE, "AUTH-003", "만료된 설정 토큰입니다."),
    LAST_ACTIVE_MASTER_ADMIN_REQUIRED(HttpStatus.CONFLICT, "AUTH-004", "마지막 활성 총괄관리자는 비활성화할 수 없습니다."),
    ROLE_SCOPE_RESTRICTED(HttpStatus.FORBIDDEN, "AUTH-005", "REQ-003 범위를 벗어난 역할입니다."),
    DUPLICATE_WORKER_CODE(HttpStatus.CONFLICT, "AUTH-006", "이미 사용 중인 작업자 코드입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH-007", "이미 사용 중인 이메일입니다."),
    INVALID_REFERENCE(HttpStatus.BAD_REQUEST, "AUTH-008", "유효하지 않은 참조값입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
