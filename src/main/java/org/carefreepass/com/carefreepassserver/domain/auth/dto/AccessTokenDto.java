package org.carefreepass.com.carefreepassserver.domain.auth.dto;

import lombok.Builder;
import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;

@Builder
public record AccessTokenDto(
        Long memberId,
        MemberRole memberRole,
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
