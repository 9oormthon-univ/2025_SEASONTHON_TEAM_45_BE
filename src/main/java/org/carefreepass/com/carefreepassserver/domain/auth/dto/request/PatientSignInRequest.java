package org.carefreepass.com.carefreepassserver.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PatientSignInRequest(

        @Schema(description = "전화번호(하이픈 포함 X)", example = "01012345678")
        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^01[0-9]\\d{4}\\d{4}$", message = "전화번호는 01012345678 형식이어야 합니다")
        String phoneNumber,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {
}
