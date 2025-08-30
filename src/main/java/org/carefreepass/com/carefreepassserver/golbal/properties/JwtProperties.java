package org.carefreepass.com.carefreepassserver.golbal.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String accessTokenSecret,
        Long accessTokenExpirationTime,
        String refreshTokenSecret,
        Long refreshTokenExpirationTime,
        String temporaryTokenSecret,
        Long temporaryTokenExpirationTime
) {
    public Long accessTokenExpirationMilliTime() {
        return accessTokenExpirationTime * 1000;
    }

    public Long refreshTokenExpirationMilliTime() {
        return refreshTokenExpirationTime * 1000;
    }
}
