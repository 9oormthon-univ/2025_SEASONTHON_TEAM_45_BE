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
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
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
        Long appointmentId = appointmentService.createAppointment(request);
        return ApiResponseTemplate.ok()
                .code("APPOINTMENT_4001")
                .message("예약이 성공적으로 생성되었습니다.")
                .body(appointmentId);
    }

    @Override
    @PutMapping("/checkin")
    public ApiResponseTemplate<String> checkinAppointment(@Valid @RequestBody AppointmentCheckinRequest request) {
        appointmentService.checkinAppointment(request.getAppointmentId(), request.getMemberId());
        return ApiResponseTemplate.ok()
                .code("APPOINTMENT_4002")
                .message("체크인이 성공적으로 완료되었습니다.")
                .body("SUCCESS");
    }

    @Override
    @GetMapping("/today/waiting")
    public ApiResponseTemplate<List<AppointmentResponse>> getTodayWaitingPatients() {
        List<Appointment> appointments = appointmentService.getTodayWaitingPatients();
        List<AppointmentResponse> responses = appointments.stream()
                .map(AppointmentResponse::from)
                .toList();
        return ApiResponseTemplate.ok()
                .code("APPOINTMENT_4003")
                .message("오늘 대기 환자 목록 조회가 완료되었습니다.")
                .body(responses);
    }

    @Override
    @GetMapping("/today")
    public ApiResponseTemplate<List<AppointmentResponse>> getAllTodayAppointments() {
        List<Appointment> appointments = appointmentService.getAllTodayAppointments();
        List<AppointmentResponse> responses = appointments.stream()
                .map(AppointmentResponse::from)
                .toList();
        return ApiResponseTemplate.ok()
                .code("APPOINTMENT_4004")
                .message("오늘 예약 목록 조회가 완료되었습니다.")
                .body(responses);
    }

    @Override
    @DeleteMapping("/{appointmentId}")
    public ApiResponseTemplate<String> deleteAppointment(@PathVariable Long appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ApiResponseTemplate.ok()
                .code("APPOINTMENT_4005")
                .message("예약이 성공적으로 삭제되었습니다.")
                .body("SUCCESS");
    }

    @Override
    @PutMapping("/{appointmentId}/status/{status}")
    public ApiResponseTemplate<String> updateAppointmentStatus(@PathVariable Long appointmentId, @PathVariable String status) {
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        appointmentService.updateAppointmentStatus(appointmentId, appointmentStatus);
        return ApiResponseTemplate.ok()
                .code("APPOINTMENT_4006")
                .message("예약 상태가 성공적으로 변경되었습니다.")
                .body("SUCCESS");
    }

    @Override
    @PutMapping("/{appointmentId}")
    public ApiResponseTemplate<String> updateAppointment(@PathVariable Long appointmentId, 
                                                       @Valid @RequestBody AppointmentUpdateRequest request) {
        appointmentService.updateAppointment(appointmentId, request);
        return ApiResponseTemplate.ok()
                .code("APPOINTMENT_4007")
                .message("예약이 성공적으로 수정되었습니다.")
                .body("SUCCESS");
    }

}