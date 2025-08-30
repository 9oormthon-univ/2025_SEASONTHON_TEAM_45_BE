package org.carefreepass.com.carefreepassserver.domain.appointment.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Transactional
    public Long createAppointment(Long memberId, String hospitalName, String department,
                                String doctorName, LocalDate appointmentDate, LocalTime appointmentTime, String roomNumber) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (appointmentRepository.existsByMemberIdAndAppointmentDateAndStatus(memberId, appointmentDate, AppointmentStatus.BOOKED)) {
            throw new IllegalStateException("해당 날짜에 이미 예약이 있습니다.");
        }

        Appointment appointment = Appointment.createAppointment(
                member, hospitalName, department, doctorName, appointmentDate, appointmentTime, roomNumber
        );

        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.sendAppointmentConfirmation(
                memberId, member.getName(), hospitalName, appointmentDate.toString(), appointmentTime.toString()
        );

        log.info("Appointment created for member: {} (ID: {})", member.getName(), memberId);
        return savedAppointment.getId();
    }

    @Transactional
    public void checkinAppointment(Long appointmentId, Long memberId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!appointment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 체크인할 수 있습니다.");
        }

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("예약 상태가 체크인 가능한 상태가 아닙니다.");
        }

        appointment.checkin();
        log.info("Patient checked in: {} (Appointment ID: {})", appointment.getMember().getName(), appointmentId);
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

    @Transactional
    public void callPatient(Long appointmentId, String customRoomNumber) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!appointment.canCall()) {
            throw new IllegalStateException("호출할 수 없는 예약 상태입니다.");
        }

        String roomNumber = customRoomNumber != null ? customRoomNumber : appointment.getRoomNumber();
        
        boolean success = notificationService.sendPatientCall(
                appointment.getMember().getId(), 
                appointment.getMember().getName(), 
                roomNumber,
                appointment.getId()
        );

        if (success) {
            appointment.call();
            log.info("Patient called successfully: {} (Appointment ID: {})", 
                    appointment.getMember().getName(), appointmentId);
        } else {
            log.error("Failed to call patient: {} (Appointment ID: {})", 
                    appointment.getMember().getName(), appointmentId);
            throw new RuntimeException("푸시 알림 전송에 실패했습니다.");
        }
    }

    public Appointment getAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
    }
}