package org.carefreepass.com.carefreepassserver.domain.hospital.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HospitalDepartmentResponse {

    @Schema(description = "진료과 ID", example = "1")
    private Long departmentId;

    @Schema(description = "진료과명", example = "내과")
    private String name;

    @Schema(description = "진료과 설명", example = "일반적인 내과 진료를 담당합니다.")
    private String description;

    @Schema(description = "기본 진료 시작 시간", example = "10:00")
    private LocalTime defaultStartTime;

    @Schema(description = "기본 진료 종료 시간", example = "16:30")
    private LocalTime defaultEndTime;

    @Schema(description = "예약 슬롯 간격(분)", example = "30")
    private Integer slotDurationMinutes;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean active;

    public static HospitalDepartmentResponse from(HospitalDepartment hospitalDepartment) {
        return new HospitalDepartmentResponse(
                hospitalDepartment.getId(),
                hospitalDepartment.getName(),
                hospitalDepartment.getDescription(),
                hospitalDepartment.getDefaultStartTime(),
                hospitalDepartment.getDefaultEndTime(),
                hospitalDepartment.getSlotDurationMinutes(),
                hospitalDepartment.getActive()
        );
    }
}