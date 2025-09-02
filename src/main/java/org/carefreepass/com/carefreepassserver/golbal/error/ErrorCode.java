package org.carefreepass.com.carefreepassserver.golbal.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========== 인증/인가 관련 ==========
    // 로그인 필요
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "로그인이 필요한 서비스입니다. 다시 로그인해주세요."),
    // 권한 없음
    FORBIDDEN(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다. 관리자에게 문의하세요."),
    // 토큰 만료/무효
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "로그인이 만료되었습니다. 다시 로그인해주세요."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "인증 토큰이 만료되었습니다. 다시 로그인해주세요."),
    
    // ========== 회원 관련 ==========
    // 회원가입 오류
    UNKNOWN_GENDER(HttpStatus.BAD_REQUEST, "INVALID_GENDER", "성별을 올바르게 선택해주세요. (남성/여성)"),
    ALREADY_REGISTERED_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "PHONE_ALREADY_EXISTS", "이미 가입된 전화번호입니다. 다른 번호로 시도해주세요."),
    INVALID_PHONE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_PHONE_FORMAT", "전화번호 형식이 올바르지 않습니다. (예: 01012345678)"),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_FORMAT", "비밀번호는 영문, 숫자, 특수문자를 포함하여 8-20자로 입력해주세요."),
    INVALID_TEMPORARY_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4015", "유효하지 않은 임시 토큰입니다."),
    TEMPORARY_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_4014", "임시 토큰이 만료되었습니다."),
    TEMPORARY_MEMBER_MISMATCH(HttpStatus.BAD_REQUEST, "TEMPORARY_MEMBER_MISMATCH", "임시 회원 정보와 일치하지 않는 전화번호입니다."),

    // 로그인 오류
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "등록되지 않은 전화번호입니다. 회원가입을 먼저 진행해주세요."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 틀렸습니다. 다시 확인해주세요."),
    
    // ========== 예약 관련 ==========
    // 예약 조회 실패
    APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND", "예약 정보를 찾을 수 없습니다. 예약 목록을 다시 확인해주세요."),
    
    // 예약 생성 실패
    APPOINTMENT_DUPLICATE_DATE(HttpStatus.CONFLICT, "APPOINTMENT_DUPLICATE_DATE", "해당 날짜에 이미 예약이 있습니다. 다른 날짜를 선택해주세요."),
    APPOINTMENT_TIME_UNAVAILABLE(HttpStatus.CONFLICT, "APPOINTMENT_TIME_UNAVAILABLE", "선택하신 시간은 이미 예약되었습니다. 다른 시간을 선택해주세요."),
    APPOINTMENT_PAST_DATE(HttpStatus.BAD_REQUEST, "APPOINTMENT_PAST_DATE", "과거 날짜로는 예약할 수 없습니다. 오늘 이후 날짜를 선택해주세요."),
    APPOINTMENT_TOO_LATE_BOOKING(HttpStatus.BAD_REQUEST, "APPOINTMENT_TOO_LATE", "예약은 최소 1시간 전에 해야 합니다."),
    
    // 예약 수정/취소 실패
    APPOINTMENT_CANNOT_MODIFY_COMPLETED(HttpStatus.BAD_REQUEST, "APPOINTMENT_COMPLETED", "완료된 예약은 수정할 수 없습니다."),
    APPOINTMENT_CANNOT_MODIFY_CANCELLED(HttpStatus.BAD_REQUEST, "APPOINTMENT_CANCELLED", "취소된 예약은 수정할 수 없습니다."),
    APPOINTMENT_CANNOT_CANCEL_STARTED(HttpStatus.BAD_REQUEST, "APPOINTMENT_ALREADY_STARTED", "진료가 시작된 예약은 취소할 수 없습니다."),
    
    // 환자 호출 관련
    APPOINTMENT_CALL_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "CALL_NOT_AVAILABLE", "현재 호출할 수 없는 상태입니다. 잠시 후 다시 시도해주세요."),
    APPOINTMENT_PATIENT_NOT_ARRIVED(HttpStatus.BAD_REQUEST, "PATIENT_NOT_ARRIVED", "환자가 아직 도착하지 않았습니다."),
    
    // ========== 채팅 관련 ==========
    // 세션 관련
    CHAT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_SESSION_NOT_FOUND", "채팅 기록을 찾을 수 없습니다. 새로운 상담을 시작해주세요."),
    CHAT_SESSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_ACCESS_DENIED", "다른 사용자의 채팅에는 접근할 수 없습니다."),
    CHAT_SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "CHAT_SESSION_EXPIRED", "채팅 세션이 만료되었습니다. 새로운 상담을 시작해주세요."),
    
    // AI 서비스 관련
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_DOWN", "AI 상담 서비스가 일시적으로 중단되었습니다. 잠시 후 다시 시도해주세요."),
    AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_ANALYSIS_FAILED", "증상 분석에 실패했습니다. 다시 시도해주세요."),
    CHAT_MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "MESSAGE_TOO_LONG", "메시지가 너무 깁니다. 500자 이내로 입력해주세요."),
    CHAT_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "MESSAGE_EMPTY", "메시지를 입력해주세요."),
    
    // ========== 알림 관련 ==========
    // 디바이스 토큰 관련
    DEVICE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "DEVICE_TOKEN_NOT_FOUND", "알림 설정이 필요합니다. 앱 설정에서 알림을 허용해주세요."),
    DEVICE_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "DEVICE_TOKEN_EXPIRED", "알림 설정이 만료되었습니다. 앱을 다시 실행해주세요."),
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_FCM_TOKEN", "유효하지 않은 디바이스 정보입니다. 앱을 다시 설치해주세요."),
    
    // 알림 전송 실패
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_FAILED", "알림 전송에 실패했습니다. 네트워크 연결을 확인해주세요."),
    NOTIFICATION_PERMISSION_DENIED(HttpStatus.BAD_REQUEST, "NOTIFICATION_DENIED", "알림 권한이 거부되어 있습니다. 설정에서 알림을 허용해주세요."),
    
    // ========== 입력값 검증 ==========
    // 필수 입력값 누락
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "REQUIRED_FIELD_MISSING", "필수 입력 항목이 누락되었습니다. 모든 항목을 입력해주세요."),
    
    // 형식 오류
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_DATE_FORMAT", "날짜 형식이 올바르지 않습니다. (예: 2024-12-31)"),
    INVALID_TIME_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_TIME_FORMAT", "시간 형식이 올바르지 않습니다. (예: 14:30)"),
    INVALID_NAME_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_NAME_FORMAT", "이름은 2-50자 사이로 입력해주세요."),
    INVALID_HOSPITAL_NAME(HttpStatus.BAD_REQUEST, "INVALID_HOSPITAL_NAME", "병원명을 올바르게 입력해주세요."),
    INVALID_DEPARTMENT(HttpStatus.BAD_REQUEST, "INVALID_DEPARTMENT", "진료과를 올바르게 선택해주세요."),
    
    // 범위 초과
    INPUT_TOO_LONG(HttpStatus.BAD_REQUEST, "INPUT_TOO_LONG", "입력값이 허용된 길이를 초과했습니다."),
    INPUT_TOO_SHORT(HttpStatus.BAD_REQUEST, "INPUT_TOO_SHORT", "입력값이 너무 짧습니다."),
    
    // ========== 서버 오류 ==========
    // 일반적인 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "서비스가 일시적으로 중단되었습니다. 잠시 후 다시 시도해주세요."),
    
    // 데이터베이스 관련
    DATABASE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB_CONNECTION_ERROR", "데이터베이스 연결에 실패했습니다. 잠시 후 다시 시도해주세요."),
    DATABASE_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "DB_TIMEOUT", "요청 처리 시간이 초과되었습니다. 다시 시도해주세요."),

    // 문자 인증 관련
    SMS_VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "SMS_4040", "문자 인증 코드가 존재하지 않습니다."),
    SMS_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "SMS_4000", "문자 인증 코드가 일치하지 않습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS_5000", "SMS 전송에 실패했습니다."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "SMS_4001", "유효하지 않은 전화번호 형식입니다."),
    PHONE_NUMBER_REQUIRED(HttpStatus.BAD_REQUEST, "SMS_4002", "전화번호는 필수입니다."),

    // 외부 API 관련
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_ERROR", "외부 서비스 연결에 실패했습니다. 잠시 후 다시 시도해주세요."),
    OPENAI_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "OPENAI_API_ERROR", "AI 서비스 연결에 실패했습니다. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
