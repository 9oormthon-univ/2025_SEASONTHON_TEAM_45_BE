package org.carefreepass.com.carefreepassserver.domain.appointment.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.AppointmentStatus;
import org.carefreepass.com.carefreepassserver.domain.appointment.repository.AppointmentRepository;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.domain.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예약 관리 서비스
 * 환자의 병원 예약 생성, 수정, 삭제, 조회 및 호출 기능을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    /**
     * 새로운 예약을 생성합니다.
     * 
     * @param memberId 환자 ID
     * @param hospitalName 병원명
     * @param department 진료과
     * @param doctorName 의사명
     * @param appointmentDate 예약 날짜
     * @param appointmentTime 예약 시간
     * @param roomNumber 진료실 번호
     * @return 생성된 예약 ID
     * @throws IllegalArgumentException 존재하지 않는 회원인 경우
     * @throws IllegalStateException 해당 날짜에 이미 예약이 있는 경우
     */
    @Transactional
    public Long createAppointment(Long memberId, String hospitalName, String department,
                                String doctorName, LocalDate appointmentDate, LocalTime appointmentTime, String roomNumber) {
        // 회원 존재 여부 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 1. 환자별 중복 예약 검증 (같은 날짜에 예약된 상태인 예약이 있는지 확인)
        if (appointmentRepository.existsByMemberIdAndAppointmentDateAndStatus(memberId, appointmentDate, AppointmentStatus.BOOKED)) {
            throw new IllegalStateException("해당 날짜에 이미 예약이 있습니다.");
        }

        // 2. 의사별 시간 충돌 검사
        if (appointmentRepository.existsByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
                doctorName, appointmentDate, appointmentTime, AppointmentStatus.BOOKED)) {
            throw new IllegalStateException(String.format("의사 '%s'는 %s %s 시간에 이미 예약이 있습니다.", 
                    doctorName, appointmentDate, appointmentTime));
        }

        // 3. 진료실별 시간 충돌 검사  
        if (appointmentRepository.existsByRoomNumberAndAppointmentDateAndAppointmentTimeAndStatus(
                roomNumber, appointmentDate, appointmentTime, AppointmentStatus.BOOKED)) {
            throw new IllegalStateException(String.format("진료실 '%s'는 %s %s 시간에 이미 예약이 있습니다.", 
                    roomNumber, appointmentDate, appointmentTime));
        }

        // 예약 엔티티 생성 (초기 상태: BOOKED)
        Appointment appointment = Appointment.createAppointment(
                member, hospitalName, department, doctorName, appointmentDate, appointmentTime, roomNumber
        );

        // 예약 저장
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 예약 확인 알림 전송
        notificationService.sendAppointmentConfirmation(
                savedAppointment.getId(), memberId, member.getName(), hospitalName, appointmentDate.toString(), appointmentTime.toString()
        );

        log.info("예약 생성 완료: 회원 {} (ID: {})", member.getName(), memberId);
        return savedAppointment.getId();
    }

    /**
     * 환자 체크인을 처리합니다.
     * BOOKED 상태의 예약을 ARRIVED 상태로 변경합니다.
     * 
     * @param appointmentId 예약 ID
     * @param memberId 환자 ID
     * @throws IllegalArgumentException 존재하지 않는 예약이거나 본인 예약이 아닌 경우
     * @throws IllegalStateException 체크인 불가능한 상태인 경우
     */
    @Transactional
    public void checkinAppointment(Long appointmentId, Long memberId) {
        // 예약 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 본인 예약인지 확인
        if (!appointment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 체크인할 수 있습니다.");
        }

        // 체크인 가능한 상태인지 확인 (BOOKED 상태에서만 체크인 가능)
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("예약 상태가 체크인 가능한 상태가 아닙니다.");
        }

        // 체크인 처리 (상태를 ARRIVED로 변경)
        appointment.checkin();
        log.info("환자 체크인 완료: {} (예약 ID: {})", appointment.getMember().getName(), appointmentId);
    }

    public List<Appointment> getTodayWaitingPatients() {
        List<AppointmentStatus> waitingStatuses = Arrays.asList(
                AppointmentStatus.BOOKED,
                AppointmentStatus.ARRIVED
        );

        return appointmentRepository.findTodayAppointmentsByStatus(LocalDate.now(), waitingStatuses);
    }

    public List<Appointment> getAllTodayAppointments() {
        return appointmentRepository.findAllByAppointmentDate(LocalDate.now());
    }

    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
        
        appointmentRepository.delete(appointment);
        log.info("Appointment deleted: {} (ID: {})", appointment.getMember().getName(), appointmentId);
    }

    @Transactional
    public void updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        appointment.updateStatus(status);
        log.info("Appointment status updated: {} -> {} (ID: {})", 
                appointment.getStatus(), status, appointmentId);
    }

    @Transactional
    public void updateAppointment(Long appointmentId, String hospitalName, String department, 
                                String doctorName, LocalDate appointmentDate, LocalTime appointmentTime, String roomNumber) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("완료되거나 취소된 예약은 수정할 수 없습니다.");
        }

        appointment.updateAppointment(hospitalName, department, doctorName, appointmentDate, appointmentTime, roomNumber);
        log.info("Appointment updated: {} (ID: {})", appointment.getMember().getName(), appointmentId);
    }

    /**
     * 환자를 호출합니다. (핵심 기능)
     * FCM을 통해 환자에게 진료실 호출 알림을 전송하고, 성공 시 예약 상태를 CALLED로 변경합니다.
     * 
     * @param appointmentId 예약 ID
     * @param customRoomNumber 사용자 지정 진료실 번호 (null인 경우 예약의 기본 진료실 사용)
     * @throws IllegalArgumentException 존재하지 않는 예약인 경우
     * @throws IllegalStateException 호출 불가능한 상태인 경우 (COMPLETED, CANCELLED)
     * @throws RuntimeException 푸시 알림 전송 실패인 경우
     */
    @Transactional
    public void callPatient(Long appointmentId, String customRoomNumber) {
        // 예약 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 호출 가능한 상태인지 확인 (COMPLETED, CANCELLED 제외)
        if (!appointment.canCall()) {
            throw new IllegalStateException("호출할 수 없는 예약 상태입니다.");
        }

        // 진료실 번호 결정 (사용자 지정 > 예약 기본값)
        String roomNumber = customRoomNumber != null ? customRoomNumber : appointment.getRoomNumber();
        
        // FCM 푸시 알림 전송 시도
        boolean success = notificationService.sendPatientCall(
                appointment.getMember().getId(), 
                appointment.getMember().getName(), 
                roomNumber,
                appointment.getId()
        );

        if (success) {
            // 알림 전송 성공 시 예약 상태를 CALLED로 변경
            appointment.call();
            log.info("Patient called successfully: {} (Appointment ID: {})", 
                    appointment.getMember().getName(), appointmentId);
        } else {
            // 알림 전송 실패 시 예외 발생
            log.error("Failed to call patient: {} (Appointment ID: {})", 
                    appointment.getMember().getName(), appointmentId);
            throw new RuntimeException("푸시 알림 전송에 실패했습니다.");
        }
    }

    public Appointment getAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
    }

    /**
     * 의사별 예약 가능 시간을 조회합니다.
     * 
     * @param doctorName 의사 이름
     * @param appointmentDate 예약 날짜
     * @return 예약 가능한 시간 목록
     */
    public List<LocalTime> getAvailableTimesByDoctor(String doctorName, LocalDate appointmentDate) {
        // 기본 진료 시간 (09:00 ~ 17:00, 30분 간격)
        List<LocalTime> allTimes = generateTimeSlots(LocalTime.of(9, 0), LocalTime.of(17, 0), 30);
        
        // 이미 예약된 시간 조회
        List<LocalTime> bookedTimes = appointmentRepository.findBookedTimesByDoctorAndDate(
                doctorName, appointmentDate, AppointmentStatus.BOOKED);
        
        // 예약 가능한 시간 = 전체 시간 - 예약된 시간
        List<LocalTime> availableTimes = new ArrayList<>(allTimes);
        availableTimes.removeAll(bookedTimes);
        
        log.info("의사 '{}' {}일 예약 가능 시간: {}개", doctorName, appointmentDate, availableTimes.size());
        return availableTimes;
    }

    /**
     * 진료실별 예약 가능 시간을 조회합니다.
     * 
     * @param roomNumber 진료실 번호
     * @param appointmentDate 예약 날짜
     * @return 예약 가능한 시간 목록
     */
    public List<LocalTime> getAvailableTimesByRoom(String roomNumber, LocalDate appointmentDate) {
        // 기본 진료 시간 (09:00 ~ 17:00, 30분 간격)
        List<LocalTime> allTimes = generateTimeSlots(LocalTime.of(9, 0), LocalTime.of(17, 0), 30);
        
        // 이미 예약된 시간 조회
        List<LocalTime> bookedTimes = appointmentRepository.findBookedTimesByRoomAndDate(
                roomNumber, appointmentDate, AppointmentStatus.BOOKED);
        
        // 예약 가능한 시간 = 전체 시간 - 예약된 시간
        List<LocalTime> availableTimes = new ArrayList<>(allTimes);
        availableTimes.removeAll(bookedTimes);
        
        log.info("진료실 '{}' {}일 예약 가능 시간: {}개", roomNumber, appointmentDate, availableTimes.size());
        return availableTimes;
    }

    /**
     * 지정된 시간 범위와 간격으로 시간 슬롯을 생성합니다.
     * 
     * @param startTime 시작 시간
     * @param endTime 종료 시간  
     * @param intervalMinutes 간격(분)
     * @return 시간 슬롯 목록
     */
    private List<LocalTime> generateTimeSlots(LocalTime startTime, LocalTime endTime, int intervalMinutes) {
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime current = startTime;
        
        while (current.isBefore(endTime)) {
            timeSlots.add(current);
            current = current.plusMinutes(intervalMinutes);
        }
        
        return timeSlots;
    }
}