package org.carefreepass.com.carefreepassserver.domain.hospital.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.TimeSlotException;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TimeSlotExceptionResponse {

    @Schema(description = "시간 예외 ID", example = "1")
    private Long exceptionId;

    @Schema(description = "진료과 ID", example = "1")
    private Long departmentId;

    @Schema(description = "진료과명", example = "내과")
    private String departmentName;

    @Schema(description = "예외 날짜", example = "2025-09-04")
    private LocalDate exceptionDate;

    @Schema(description = "예외 시간", example = "14:00")
    private LocalTime exceptionTime;

    @Schema(description = "차단 여부", example = "true")
    private Boolean blocked;

    public static TimeSlotExceptionResponse from(TimeSlotException timeSlotException) {
        return new TimeSlotExceptionResponse(
                timeSlotException.getId(),
                timeSlotException.getHospitalDepartment().getId(),
                timeSlotException.getHospitalDepartment().getName(),
                timeSlotException.getExceptionDate(),
                timeSlotException.getExceptionTime(),
                timeSlotException.getBlocked()
        );
    }
}