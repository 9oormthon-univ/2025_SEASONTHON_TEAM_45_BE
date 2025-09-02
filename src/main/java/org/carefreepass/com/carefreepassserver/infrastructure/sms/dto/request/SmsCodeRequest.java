package org.carefreepass.com.carefreepassserver.infrastructure.sms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SmsCodeRequest(

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phoneNumber,

        @NotBlank(message = "인증 코드는 필수입니다.")
        @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
        String code
) {
}
