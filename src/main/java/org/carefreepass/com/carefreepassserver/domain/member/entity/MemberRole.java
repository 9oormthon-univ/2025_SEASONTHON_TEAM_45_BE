package org.carefreepass.com.carefreepassserver.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    HOSPITAL("ROLE_HOSPITAL"),
    TEMPORARY("ROLE_TEMPORARY"),
    ;

    private final String value;
}
