package org.carefreepass.com.carefreepassserver.golbal.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_4010", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_4030", "접근 권한이 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4011", "리프레시 토큰이 유효하지 않습니다."),
    UNKNOWN_GENDER(HttpStatus.BAD_REQUEST, "MEMBER_4001", "알 수 없는 성별입니다."),
    ALREADY_REGISTERED_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "MEMBER_4002", "이미 등록된 전화번호입니다."),

    // 회원 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_4041", "회원을 찾을 수 없습니다."),

    // 잘못된 요청
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "REQ_4000", "잘못된 요청입니다."),

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SRV_5000", "서버 내부 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SRV_5030", "현재 서비스를 사용할 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
