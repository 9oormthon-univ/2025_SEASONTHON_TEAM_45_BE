package org.carefreepass.com.carefreepassserver.domain.hospital.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;

/**
 * 진료과 목록 조회용 간단한 응답 DTO
 * 환자가 예약 시 필요한 최소한의 진료과 정보만 포함합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DepartmentListResponse {

    @Schema(description = "진료과 ID", example = "1")
    private Long departmentId;

    @Schema(description = "진료과명", example = "내과")
    private String name;

    @Schema(description = "진료과 설명", example = "일반적인 내과 진료를 담당합니다.")
    private String description;

    @Schema(description = "병원명", example = "구름대병원")
    private String hospitalName;

    public static DepartmentListResponse from(HospitalDepartment hospitalDepartment) {
        return new DepartmentListResponse(
                hospitalDepartment.getId(),
                hospitalDepartment.getName(),
                hospitalDepartment.getDescription(),
                hospitalDepartment.getHospital().getName()
        );
    }
}