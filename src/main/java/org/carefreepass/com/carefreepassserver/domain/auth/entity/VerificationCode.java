package org.carefreepass.com.carefreepassserver.domain.auth.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "phoneNumber")
@RedisHash(value = "verificationCode")
public class VerificationCode {

    @Id
    private String phoneNumber;

    private String code;

    @TimeToLive
    private long ttl;

    @Builder(access = AccessLevel.PRIVATE)
    private VerificationCode(final String phoneNumber, final String code, final long ttl) {
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.ttl = ttl;
    }

    public static VerificationCode createVerificationCode(final String phoneNumber, final String code, final long ttl) {
        return VerificationCode.builder()
                .phoneNumber(phoneNumber)
                .code(code)
                .ttl(ttl)
                .build();
    }
}