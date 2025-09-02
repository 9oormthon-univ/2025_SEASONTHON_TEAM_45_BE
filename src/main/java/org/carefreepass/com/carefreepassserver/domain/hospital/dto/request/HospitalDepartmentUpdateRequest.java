package org.carefreepass.com.carefreepassserver.domain.hospital.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HospitalDepartmentUpdateRequest {

    @Schema(description = "진료과명", example = "내과")
    @NotBlank(message = "진료과명은 필수입니다.")
    @Size(max = 50, message = "진료과명은 50자 이내여야 합니다.")
    private String name;

    @Schema(description = "진료과 설명", example = "일반적인 내과 진료를 담당합니다.")
    @Size(max = 200, message = "진료과 설명은 200자 이내여야 합니다.")
    private String description;
}