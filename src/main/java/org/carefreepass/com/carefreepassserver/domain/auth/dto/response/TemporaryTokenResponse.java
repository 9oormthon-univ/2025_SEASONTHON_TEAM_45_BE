package org.carefreepass.com.carefreepassserver.domain.auth.dto.response;

import lombok.Builder;

@Builder
public record TemporaryTokenResponse(
        String temporaryToken
) {
    public static TemporaryTokenResponse from(final String temporaryToken) {
        return TemporaryTokenResponse.builder()
                .temporaryToken(temporaryToken)
                .build();
    }
}
