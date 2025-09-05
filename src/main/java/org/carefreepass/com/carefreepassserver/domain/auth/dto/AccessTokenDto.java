package org.carefreepass.com.carefreepassserver.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.MemberRole;

@Builder
public record AccessTokenDto(
        @Schema(description = "회원 ID", example = "1")
        Long memberId,
        
        @Schema(description = "회원 권한", example = "PATIENT")
        MemberRole memberRole,
        
        @Schema(description = "액세스 토큰 값", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String tokenValue
) {
    public static AccessTokenDto of(final Long memberId, final MemberRole memberRole, final String tokenValue) {
        return AccessTokenDto.builder()
                .memberId(memberId)
                .memberRole(memberRole)
                .tokenValue(tokenValue)
                .build();
    }
}
