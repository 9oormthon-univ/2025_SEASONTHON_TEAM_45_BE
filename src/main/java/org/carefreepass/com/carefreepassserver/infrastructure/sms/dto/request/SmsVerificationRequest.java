package org.carefreepass.com.carefreepassserver.infrastructure.sms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SmsVerificationRequest(
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phoneNumber
) {
}
