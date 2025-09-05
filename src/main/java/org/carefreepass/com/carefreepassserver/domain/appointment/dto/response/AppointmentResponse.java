package org.carefreepass.com.carefreepassserver.domain.appointment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.PatientProfile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AppointmentResponse {

    @Schema(description = "예약 ID", example = "1")
    private Long appointmentId;
    
    @Schema(description = "회원명", example = "김환자")
    private String memberName;
    
    @Schema(description = "회원 전화번호", example = "010-1234-5678")
    private String memberPhoneNumber;
    
    @Schema(description = "회원 생년월일", example = "1990-01-15")
    private LocalDate memberBirthDate;
    
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
                appointment.getMember().getPhoneNumber(),
                null, // birthDate는 PatientProfile이 필요하므로 null로 설정
                appointment.getHospitalName(),
                appointment.getDepartmentName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus().name(),
                appointment.getStatus().getDescription(),
                appointment.canCall()
        );
    }

    public static AppointmentResponse from(Appointment appointment, PatientProfile patientProfile) {
        LocalDate birthDate = null;
        
        // PatientProfile이 있고 birthDate가 있으면 변환 시도
        if (patientProfile != null && patientProfile.getBirthDate() != null) {
            try {
                // birthDate가 문자열로 저장되어 있다면 LocalDate로 변환
                birthDate = LocalDate.parse(patientProfile.getBirthDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                // 파싱 실패 시 null로 설정 (로그는 생략)
                birthDate = null;
            }
        }
        
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getMember().getName(),
                appointment.getMember().getPhoneNumber(),
                birthDate,
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