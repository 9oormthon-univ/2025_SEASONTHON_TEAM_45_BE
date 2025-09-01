package org.carefreepass.com.carefreepassserver.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenPairResponse(
        @Schema(description = "액세스 토큰", defaultValue = "accessToken") String accessToken,
        @Schema(description = "리프레시 토큰", defaultValue = "refreshToken") String refreshToken,
        @Schema(description = "회원 ID", example = "1") Long memberId,
        @Schema(description = "회원 이름", example = "김철수") String memberName,
        @Schema(description = "회원 역할", example = "PATIENT") String role
) {
    public static TokenPairResponse of(final String accessToken, final String refreshToken, final Long memberId, final String memberName, final String role) {
        return new TokenPairResponse(accessToken, refreshToken, memberId, memberName, role);
    }
    
    // 기존 메서드 호환성을 위한 오버로드
    public static TokenPairResponse of(final String accessToken, final String refreshToken) {
        return new TokenPairResponse(accessToken, refreshToken, null, null, null);
    }
}