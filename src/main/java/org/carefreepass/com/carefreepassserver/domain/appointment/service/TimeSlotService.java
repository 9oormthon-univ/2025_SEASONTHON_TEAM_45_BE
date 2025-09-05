package org.carefreepass.com.carefreepassserver.domain.appointment.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.TimeSlotResponse;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.AppointmentStatus;
import org.carefreepass.com.carefreepassserver.domain.appointment.repository.AppointmentRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.Hospital;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalDepartmentRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.TimeSlotExceptionRepository;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 시간대 조회 서비스
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TimeSlotService {

    private final HospitalRepository hospitalRepository;
    private final HospitalDepartmentRepository hospitalDepartmentRepository;
    private final TimeSlotExceptionRepository timeSlotExceptionRepository;
    private final AppointmentRepository appointmentRepository;

    // 특정 날짜와 진료과의 예약 가능한 시간 조회
    public List<TimeSlotResponse> getAvailableTimeSlots(Long hospitalId, String departmentName, LocalDate date) {
        // 병원 조회
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));
        
        // 진료과 조회
        HospitalDepartment department = hospitalDepartmentRepository.findByHospitalAndNameAndActiveTrue(hospital, departmentName)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        // 1. 기본 시간대 생성 (10:00~16:30, 30분 간격)
        List<LocalTime> baseTimeSlots = generateBaseTimeSlots(
                department.getDefaultStartTime(), 
                department.getDefaultEndTime(), 
                department.getSlotDurationMinutes());

        // 2. 이미 예약된 시간 조회
        Map<LocalTime, String> bookedTimes = getBookedTimes(department, date);

        // 3. 병원에서 차단한 시간 조회
        List<LocalTime> blockedTimes = timeSlotExceptionRepository
                .findBlockedTimesByDepartmentAndDate(department, date);

        // 4. 각 시간대별 가용성 계산
        List<TimeSlotResponse> timeSlots = new ArrayList<>();
        for (LocalTime time : baseTimeSlots) {
            if (bookedTimes.containsKey(time)) {
                // 이미 예약된 시간
                timeSlots.add(TimeSlotResponse.alreadyBooked(time, bookedTimes.get(time)));
            } else if (blockedTimes.contains(time)) {
                // 병원에서 차단한 시간 (사용자에게는 "이미 예약됨"으로 표시)
                timeSlots.add(TimeSlotResponse.hospitalBlocked(time));
            } else {
                // 예약 가능한 시간
                timeSlots.add(TimeSlotResponse.available(time));
            }
        }

        log.info("시간대 조회 완료: {} {} (가능: {}/{})", 
                date, departmentName, 
                timeSlots.stream().filter(TimeSlotResponse::getAvailable).count(), 
                timeSlots.size());

        return timeSlots;
    }

    // 기본 시간대 생성
    private List<LocalTime> generateBaseTimeSlots(LocalTime startTime, LocalTime endTime, int intervalMinutes) {
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime current = startTime;
        
        while (!current.isAfter(endTime)) {
            timeSlots.add(current);
            current = current.plusMinutes(intervalMinutes);
            
            // 종료 시간을 초과하면 중단
            if (current.isAfter(endTime)) {
                break;
            }
        }
        
        return timeSlots;
    }

    // 이미 예약된 시간 조회
    private Map<LocalTime, String> getBookedTimes(HospitalDepartment department, LocalDate date) {
        // 모든 활성 상태의 예약 조회 (CANCELLED와 COMPLETED 제외)
        List<AppointmentStatus> activeStatuses = List.of(
            AppointmentStatus.WAITING,
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.ARRIVED,
            AppointmentStatus.CALLED
        );

        List<Appointment> appointments = appointmentRepository
                .findByHospitalDepartmentAndAppointmentDateAndStatusIn(department, date, activeStatuses);

        return appointments.stream()
                .collect(Collectors.toMap(
                    Appointment::getAppointmentTime,
                    appointment -> appointment.getMember().getName(),
                    (existing, replacement) -> existing // 중복 시 기존 값 사용
                ));
    }

    // 특정 시간이 예약 가능한지 확인
    public boolean isTimeSlotAvailable(Long hospitalId, String departmentName, LocalDate date, LocalTime time) {
        List<TimeSlotResponse> timeSlots = getAvailableTimeSlots(hospitalId, departmentName, date);
        
        return timeSlots.stream()
                .filter(slot -> slot.getTime().equals(time))
                .findFirst()
                .map(TimeSlotResponse::getAvailable)
                .orElse(false);
    }
}