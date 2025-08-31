package org.carefreepass.com.carefreepassserver.domain.member.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum Gender {
        MALE("남성"),
        FEMALE("여성");

        private final String value;

        public static Gender from(String value) {
                return Arrays.stream(Gender.values())
                        .filter(g -> g.getValue().equals(value))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_GENDER));
        }
}
