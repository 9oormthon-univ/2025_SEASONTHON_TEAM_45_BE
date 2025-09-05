package org.carefreepass.com.carefreepassserver.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.MemberRole;

public record RefreshTokenDto(
        @Schema(description = "회원 ID", example = "1")
        Long memberId,
        
        @Schema(description = "리프레시 토큰 값", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String tokenValue,
        
        @Schema(description = "토큰 만료시간(TTL) - 초 단위", example = "604800")
        Long ttl
) {
}
