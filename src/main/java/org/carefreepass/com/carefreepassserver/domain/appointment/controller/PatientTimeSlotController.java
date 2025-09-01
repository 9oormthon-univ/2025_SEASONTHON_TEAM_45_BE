package org.carefreepass.com.carefreepassserver.domain.appointment.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.AvailableTimeSlotsResponse;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.TimeSlotResponse;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.TimeSlotService;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 환자용 시간대 조회 컨트롤러
 * 환자가 예약 가능한 시간을 조회하는 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/patient/time-slots")
@Slf4j
public class PatientTimeSlotController {

    private final TimeSlotService timeSlotService;

    /**
     * 예약 가능한 시간대 조회
     * 
     * @param hospitalId 병원 ID
     * @param departmentName 진료과명
     * @param date 조회할 날짜
     * @return 시간대별 예약 가능 여부
     */
    @GetMapping
    public ApiResponseTemplate<AvailableTimeSlotsResponse> getAvailableTimeSlots(
            @RequestParam Long hospitalId,
            @RequestParam String departmentName,
            @RequestParam LocalDate date) {
        List<TimeSlotResponse> timeSlots = timeSlotService.getAvailableTimeSlots(hospitalId, departmentName, date);
        AvailableTimeSlotsResponse response = AvailableTimeSlotsResponse.of(date, departmentName, timeSlots);
        return ApiResponseTemplate.ok()
                .code("TIME_SLOT_1001")
                .message("예약 가능 시간 조회가 완료되었습니다.")
                .body(response);
    }

    /**
     * 특정 시간의 예약 가능 여부 확인
     * 
     * @param hospitalId 병원 ID
     * @param departmentName 진료과명
     * @param date 날짜
     * @param time 시간
     * @return 예약 가능 여부
     */
    @GetMapping("/check")
    public ApiResponseTemplate<Boolean> checkTimeSlotAvailable(
            @RequestParam Long hospitalId,
            @RequestParam String departmentName,
            @RequestParam LocalDate date,
            @RequestParam String time) {
        java.time.LocalTime localTime = java.time.LocalTime.parse(time);
        boolean available = timeSlotService.isTimeSlotAvailable(hospitalId, departmentName, date, localTime);
        return ApiResponseTemplate.ok()
                .code("TIME_SLOT_1002")
                .message("시간대 예약 가능 여부 확인이 완료되었습니다.")
                .body(available);
    }
}