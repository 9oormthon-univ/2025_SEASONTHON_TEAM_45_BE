package org.carefreepass.com.carefreepassserver.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record PatientSignUpRequest(

        @Schema(description = "이름", example = "김환자")
        @NotBlank(message = "이름은 필수입니다")
        @Length(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
        String name,

        @Schema(description = "성별 (남성/여성)", example = "남성")
        @NotBlank(message = "성별은 필수입니다")
        @Pattern(regexp = "^(남성|여성)$", message = "성별은 남성, 여성 중 하나여야 합니다")
        String gender,

        @Schema(description = "생년월일(yyyyMMdd)", example = "19900315")
        @NotBlank(message = "생년월일은 필수입니다")
        @Pattern(regexp = "^\\d{4}\\d{2}\\d{2}$", message = "생년월일은 YYYYMMDD 형식이어야 합니다")
        String birthDate,

        @Schema(description = "전화번호(하이픈 포함 X)", example = "01012345678")
        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^01[0-9]\\d{4}\\d{4}$", message = "전화번호는 01000000000 형식이어야 합니다")
        String phoneNumber,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다")
        @Length(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다")
        String password
) {
}
