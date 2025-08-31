package org.carefreepass.com.carefreepassserver.golbal.error;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "오류 코드", example = "MEMBER_NOT_FOUND")
        String code,
        
        @Schema(description = "오류 메시지", example = "해당 회원을 찾을 수 없습니다.")
        String message
) {
    public static ErrorResponse of(final String code, final String message) {
        return new ErrorResponse(code, message);
    }
}
