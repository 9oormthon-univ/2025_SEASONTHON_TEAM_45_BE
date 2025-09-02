package org.carefreepass.com.carefreepassserver.domain.appointment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "예약 ID", example = "1")
    private Long appointmentId;
    
    @Schema(description = "회원명", example = "김환자")
    private String memberName;
    
    @Schema(description = "병원명", example = "서울대학교병원")
    private String hospitalName;
    
    @Schema(description = "진료과", example = "내과")
    private String department;
    
    @Schema(description = "예약 날짜", example = "2024-12-31")
    private LocalDate appointmentDate;
    
    @Schema(description = "예약 시간", example = "14:30")
    private LocalTime appointmentTime;
    
    @Schema(description = "예약 상태", example = "SCHEDULED")
    private String status;
    
    @Schema(description = "예약 상태 설명", example = "예약 완료")
    private String statusDescription;
    
    @Schema(description = "환자 호출 가능 여부", example = "true")
    private boolean canCall;

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getMember().getName(),
                appointment.getHospitalName(),
                appointment.getDepartmentName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus().name(),
                appointment.getStatus().getDescription(),
                appointment.canCall()
        );
    }
}