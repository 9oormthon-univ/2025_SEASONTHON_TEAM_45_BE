package org.carefreepass.com.carefreepassserver.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberRole {
    USER("ROLE_USER"),
    HOSPITAL("ROLE_HOSPITAL"),
    TEMPORARY("ROLE_TEMPORARY"),
    ;

    private final String value;
}
