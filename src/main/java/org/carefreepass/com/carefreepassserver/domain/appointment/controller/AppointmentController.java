package org.carefreepass.com.carefreepassserver.domain.appointment.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.controller.docs.AppointmentDocs;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCheckinRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentUpdateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.AppointmentResponse;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.AppointmentStatus;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.AppointmentService;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/appointments")
@Slf4j
public class AppointmentController implements AppointmentDocs {

    private final AppointmentService appointmentService;

    @Override
    @PostMapping
    public ApiResponseTemplate<Long> createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        try {
            Long appointmentId = appointmentService.createAppointment(
                    request.getMemberId(),
                    request.getHospitalName(),
                    request.getDepartment(),
                    request.getDoctorName(),
                    request.getAppointmentDate(),
                    request.getAppointmentTime(),
                    request.getRoomNumber()
            );

            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2001")
                    .message("예약이 성공적으로 생성되었습니다.")
                    .body(appointmentId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_4001")
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to create appointment", e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5001")
                    .message("예약 생성에 실패했습니다.")
                    .build();
        }
    }

    @Override
    @PutMapping("/checkin")
    public ApiResponseTemplate<String> checkinAppointment(@Valid @RequestBody AppointmentCheckinRequest request) {
        try {
            appointmentService.checkinAppointment(request.getAppointmentId(), request.getMemberId());

            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2002")
                    .message("체크인이 완료되었습니다.")
                    .body("SUCCESS");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_4002")
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to checkin appointment", e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5002")
                    .message("체크인에 실패했습니다.")
                    .build();
        }
    }

    @Override
    @GetMapping("/today/waiting")
    public ApiResponseTemplate<List<AppointmentResponse>> getTodayWaitingPatients() {
        try {
            List<Appointment> appointments = appointmentService.getTodayWaitingPatients();
            List<AppointmentResponse> responses = appointments.stream()
                    .map(AppointmentResponse::from)
                    .toList();

            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2003")
                    .message("오늘 대기 환자 목록 조회가 완료되었습니다.")
                    .body(responses);

        } catch (Exception e) {
            log.error("Failed to get today waiting patients", e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5003")
                    .message("대기 환자 목록 조회에 실패했습니다.")
                    .build();
        }
    }

    @Override
    @GetMapping("/today")
    public ApiResponseTemplate<List<AppointmentResponse>> getAllTodayAppointments() {
        try {
            List<Appointment> appointments = appointmentService.getAllTodayAppointments();
            List<AppointmentResponse> responses = appointments.stream()
                    .map(AppointmentResponse::from)
                    .toList();

            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2004")
                    .message("오늘 전체 예약 목록 조회가 완료되었습니다.")
                    .body(responses);

        } catch (Exception e) {
            log.error("Failed to get today appointments", e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5004")
                    .message("예약 목록 조회에 실패했습니다.")
                    .build();
        }
    }

    @Override
    @DeleteMapping("/{appointmentId}")
    public ApiResponseTemplate<String> deleteAppointment(@PathVariable Long appointmentId) {
        try {
            appointmentService.deleteAppointment(appointmentId);
            
            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2005")
                    .message("예약이 성공적으로 삭제되었습니다.")
                    .body("SUCCESS");

        } catch (IllegalArgumentException e) {
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_4003")
                    .message(e.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("Failed to delete appointment", e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5005")
                    .message("예약 삭제에 실패했습니다.")
                    .build();
        }
    }

    @Override
    @PutMapping("/{appointmentId}/status/{status}")
    public ApiResponseTemplate<String> updateAppointmentStatus(@PathVariable Long appointmentId, @PathVariable String status) {
        try {
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
            appointmentService.updateAppointmentStatus(appointmentId, appointmentStatus);

            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2006")
                    .message("예약 상태가 성공적으로 변경되었습니다.")
                    .body("SUCCESS");

        } catch (IllegalArgumentException e) {
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_4004")
                    .message("잘못된 상태값입니다: " + status)
                    .build();
        } catch (Exception e) {
            log.error("Failed to update appointment status", e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5006")
                    .message("예약 상태 변경에 실패했습니다.")
                    .build();
        }
    }

    @Override
    @PutMapping("/{appointmentId}")
    public ApiResponseTemplate<String> updateAppointment(@PathVariable Long appointmentId, 
                                                       @Valid @RequestBody AppointmentUpdateRequest request) {
        try {
            appointmentService.updateAppointment(
                    appointmentId,
                    request.getHospitalName(),
                    request.getDepartment(),
                    request.getDoctorName(),
                    request.getAppointmentDate(),
                    request.getAppointmentTime(),
                    request.getRoomNumber()
            );

            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2007")
                    .message("예약이 성공적으로 수정되었습니다.")
                    .body("SUCCESS");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_4005")
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to update appointment", e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5007")
                    .message("예약 수정에 실패했습니다.")
                    .build();
        }
    }

    /**
     * 의사별 예약 가능 시간을 조회합니다.
     * 
     * @param doctorName 의사 이름
     * @param date 예약 날짜 (yyyy-MM-dd 형식)
     * @return 예약 가능한 시간 목록
     */
    @GetMapping("/available-times/doctor")
    public ApiResponseTemplate<List<LocalTime>> getAvailableTimesByDoctor(
            @RequestParam String doctorName,
            @RequestParam LocalDate date) {
        try {
            List<LocalTime> availableTimes = appointmentService.getAvailableTimesByDoctor(doctorName, date);
            
            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2008")
                    .message("의사별 예약 가능 시간 조회가 완료되었습니다.")
                    .body(availableTimes);

        } catch (Exception e) {
            log.error("Failed to get available times by doctor: {} for date: {}", doctorName, date, e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5008")
                    .message("의사별 예약 가능 시간 조회에 실패했습니다.")
                    .build();
        }
    }

    /**
     * 진료실별 예약 가능 시간을 조회합니다.
     * 
     * @param roomNumber 진료실 번호
     * @param date 예약 날짜 (yyyy-MM-dd 형식)
     * @return 예약 가능한 시간 목록
     */
    @GetMapping("/available-times/room")
    public ApiResponseTemplate<List<LocalTime>> getAvailableTimesByRoom(
            @RequestParam String roomNumber,
            @RequestParam LocalDate date) {
        try {
            List<LocalTime> availableTimes = appointmentService.getAvailableTimesByRoom(roomNumber, date);
            
            return ApiResponseTemplate.ok()
                    .code("APPOINTMENT_2009")
                    .message("진료실별 예약 가능 시간 조회가 완료되었습니다.")
                    .body(availableTimes);

        } catch (Exception e) {
            log.error("Failed to get available times by room: {} for date: {}", roomNumber, date, e);
            return ApiResponseTemplate.error()
                    .code("APPOINTMENT_5009")
                    .message("진료실별 예약 가능 시간 조회에 실패했습니다.")
                    .build();
        }
    }
}