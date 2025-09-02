package org.carefreepass.com.carefreepassserver.domain.appointment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AppointmentUpdateRequest {

    @Schema(description = "병원 ID", example = "1")
    @NotNull(message = "병원 ID는 필수입니다.")
    private Long hospitalId;

    @Schema(description = "진료과명", example = "내과")
    @NotBlank(message = "진료과명은 필수입니다.")
    private String departmentName;

    @Schema(description = "예약 날짜", example = "2024-12-31")
    @NotNull(message = "예약 날짜는 필수입니다.")
    private LocalDate appointmentDate;

    @Schema(description = "예약 시간", example = "14:30")
    @NotNull(message = "예약 시간은 필수입니다.")
    private LocalTime appointmentTime;
}