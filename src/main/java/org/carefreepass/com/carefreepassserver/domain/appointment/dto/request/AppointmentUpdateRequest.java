package org.carefreepass.com.carefreepassserver.domain.appointment.dto.request;

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

    @NotBlank(message = "병원명은 필수입니다.")
    private String hospitalName;

    @NotBlank(message = "진료과는 필수입니다.")
    private String department;

    @NotNull(message = "예약 날짜는 필수입니다.")
    private LocalDate appointmentDate;

    @NotNull(message = "예약 시간은 필수입니다.")
    private LocalTime appointmentTime;
}