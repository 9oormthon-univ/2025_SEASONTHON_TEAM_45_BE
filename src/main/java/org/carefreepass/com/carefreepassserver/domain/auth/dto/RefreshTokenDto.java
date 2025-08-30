package org.carefreepass.com.carefreepassserver.domain.auth.dto;

import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;

public record RefreshTokenDto(
        Long memberId,
        String tokenValue,
        Long ttl
) {
}
