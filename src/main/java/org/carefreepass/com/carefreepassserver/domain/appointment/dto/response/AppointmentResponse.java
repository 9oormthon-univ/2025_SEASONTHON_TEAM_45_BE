package org.carefreepass.com.carefreepassserver.domain.appointment.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AppointmentResponse {

    private Long appointmentId;
    private String memberName;
    private String hospitalName;
    private String department;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String statusDescription;
    private boolean canCall;

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getMember().getName(),
                appointment.getHospitalName(),
                appointment.getDepartment(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus().name(),
                appointment.getStatus().getDescription(),
                appointment.canCall()
        );
    }
}