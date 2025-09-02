package org.carefreepass.com.carefreepassserver.domain.auth.entity;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@EqualsAndHashCode(of = "id")
@RedisHash(value = "temporaryMember")
public class TemporaryMember {

    @Id
    private String id;

    private String phoneNumber;

    @TimeToLive
    private final long ttl;

    @Builder(access = AccessLevel.PRIVATE)
    private TemporaryMember(final String id, final String phoneNumber, final long ttl) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.ttl = ttl;
    }

    public static TemporaryMember createTemporaryMember(final String phoneNumber) {
        return TemporaryMember.builder()
                .id(UUID.randomUUID().toString())
                .phoneNumber(phoneNumber)
                .ttl(1800)
                .build();
    }
}
