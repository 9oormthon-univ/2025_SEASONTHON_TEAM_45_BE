package org.carefreepass.com.carefreepassserver.domain.auth.entity.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@EqualsAndHashCode(of = "memberId")
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    private Long memberId;

    private final String token;

    @TimeToLive
    private final long ttl;

    @Builder
    public RefreshToken(final Long memberId, final String token, final long ttl) {
        this.memberId = memberId;
        this.token = token;
        this.ttl = ttl;
    }
}
