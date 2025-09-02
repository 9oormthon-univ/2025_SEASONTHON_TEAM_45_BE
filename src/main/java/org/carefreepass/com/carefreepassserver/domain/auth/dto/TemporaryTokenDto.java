package org.carefreepass.com.carefreepassserver.domain.auth.dto;

import org.carefreepass.com.carefreepassserver.domain.member.entity.MemberRole;

public record TemporaryTokenDto(
        String temporaryMemberId,
        MemberRole memberRole,
        String tokenValue,
        Long ttl
) {
}
