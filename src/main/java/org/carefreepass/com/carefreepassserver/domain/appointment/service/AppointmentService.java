package org.carefreepass.com.carefreepassserver.domain.appointment.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentUpdateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.AppointmentStatus;
import org.carefreepass.com.carefreepassserver.domain.appointment.repository.AppointmentRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.Hospital;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalDepartmentRepository;
import org.carefreepass.com.carefreepassserver.domain.hospital.repository.HospitalRepository;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.domain.notification.service.NotificationService;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
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
    private final HospitalRepository hospitalRepository;
    private final HospitalDepartmentRepository hospitalDepartmentRepository;
    private final NotificationService notificationService;

    /**
     * 새로운 예약을 생성합니다.
     * 
     * @param request 예약 생성 요청 정보
     * @return 생성된 예약 ID
     * @throws BusinessException 존재하지 않는 회원인 경우 (MEMBER_NOT_FOUND)
     * @throws BusinessException 존재하지 않는 병원인 경우 (HOSPITAL_NOT_FOUND)
     * @throws BusinessException 존재하지 않는 진료과인 경우 (DEPARTMENT_NOT_FOUND)
     * @throws BusinessException 해당 날짜에 이미 예약이 있는 경우 (APPOINTMENT_DUPLICATE_DATE, APPOINTMENT_TIME_UNAVAILABLE)
     */
    @Transactional
    public Long createAppointment(AppointmentCreateRequest request) {
        // 회원 존재 여부 검증
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 병원 존재 여부 검증
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        // 진료과 존재 여부 및 활성화 상태 검증
        HospitalDepartment department = hospitalDepartmentRepository
                .findByHospitalAndNameAndActiveTrue(hospital, request.getDepartmentName())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        // 1. 환자별 중복 예약 검증 (같은 날짜에 활성 상태인 예약이 있는지 확인)
        List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.WAITING,
                AppointmentStatus.BOOKED,
                AppointmentStatus.ARRIVED,
                AppointmentStatus.CALLED
        );
        
        for (AppointmentStatus status : activeStatuses) {
            if (appointmentRepository.existsByMemberIdAndAppointmentDateAndStatus(
                    request.getMemberId(), request.getAppointmentDate(), status)) {
                throw new BusinessException(ErrorCode.APPOINTMENT_DUPLICATE_DATE);
            }
        }

        // 2. 진료과별 시간 충돌 검사 (같은 진료과, 같은 시간에 활성 예약이 있는지 확인)
        for (AppointmentStatus status : activeStatuses) {
            if (appointmentRepository.existsByHospitalDepartmentAndAppointmentDateAndAppointmentTimeAndStatus(
                    department, request.getAppointmentDate(), request.getAppointmentTime(), status)) {
                throw new BusinessException(ErrorCode.APPOINTMENT_TIME_UNAVAILABLE);
            }
        }

        // 예약 엔티티 생성 (초기 상태: WAITING)
        Appointment appointment = Appointment.createAppointment(
                member, department, request.getAppointmentDate(), request.getAppointmentTime()
        );

        // 예약 저장
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 예약 확인 알림 전송
        notificationService.sendAppointmentConfirmation(
                savedAppointment.getId(), request.getMemberId(), member.getName(), 
                hospital.getName(), request.getAppointmentDate().toString(), request.getAppointmentTime().toString()
        );

        log.info("예약 생성 완료: 회원 {} (ID: {}), 진료과: {}", 
                member.getName(), request.getMemberId(), request.getDepartmentName());
        return savedAppointment.getId();
    }

    /**
     * 환자 체크인을 처리합니다.
     * BOOKED 상태의 예약을 ARRIVED 상태로 변경합니다.
     * 
     * @param appointmentId 예약 ID
     * @param memberId 환자 ID
     * @throws BusinessException 존재하지 않는 예약이거나 본인 예약이 아닌 경우 (APPOINTMENT_NOT_FOUND, FORBIDDEN)
     * @throws BusinessException 체크인 불가능한 상태인 경우 (APPOINTMENT_CANNOT_MODIFY_COMPLETED)
     */
    @Transactional
    public void checkinAppointment(Long appointmentId, Long memberId) {
        // 예약 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // 본인 예약인지 확인
        if (!appointment.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 체크인 가능한 상태인지 확인 (WAITING, BOOKED 상태에서 체크인 가능)
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CANNOT_MODIFY_COMPLETED);
        }

        // 체크인 처리 (상태를 ARRIVED로 변경)
        appointment.checkin();
        log.info("환자 체크인 완료: {} (예약 ID: {})", appointment.getMember().getName(), appointmentId);
    }

    public List<Appointment> getTodayWaitingPatients() {
        List<AppointmentStatus> waitingStatuses = Arrays.asList(
                AppointmentStatus.BOOKED,
                AppointmentStatus.WAITING,
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
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));
        
        appointmentRepository.delete(appointment);
        log.info("Appointment deleted: {} (ID: {})", appointment.getMember().getName(), appointmentId);
    }

    @Transactional
    public void updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        appointment.updateStatus(status);
        log.info("Appointment status updated: {} -> {} (ID: {})", 
                appointment.getStatus(), status, appointmentId);
    }

    @Transactional
    public void updateAppointment(Long appointmentId, AppointmentUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CANNOT_MODIFY_COMPLETED);
        }

        // 병원 존재 여부 검증
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        // 진료과 존재 여부 및 활성화 상태 검증
        HospitalDepartment department = hospitalDepartmentRepository
                .findByHospitalAndNameAndActiveTrue(hospital, request.getDepartmentName())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        appointment.updateAppointment(department, request.getAppointmentDate(), request.getAppointmentTime());
        log.info("예약 수정 완료: {} (ID: {}), 진료과: {}", 
                appointment.getMember().getName(), appointmentId, request.getDepartmentName());
    }

    /**
     * 환자를 호출합니다. (핵심 기능)
     * FCM을 통해 환자에게 진료실 호출 알림을 전송하고, 성공 시 예약 상태를 CALLED로 변경합니다.
     * 
     * @param appointmentId 예약 ID
     * @throws BusinessException 존재하지 않는 예약인 경우 (APPOINTMENT_NOT_FOUND)
     * @throws BusinessException 호출 불가능한 상태인 경우 (APPOINTMENT_CALL_NOT_AVAILABLE)
     * @throws RuntimeException 푸시 알림 전송 실패인 경우
     */
    @Transactional
    public void callPatient(Long appointmentId) {
        // 예약 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // 호출 가능한 상태인지 확인 (COMPLETED, CANCELLED 제외)
        if (!appointment.canCall()) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CALL_NOT_AVAILABLE);
        }

        // 기본 진료실 번호 사용
        String roomNumber = "진료실";
        
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
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));
    }

    // 환자용 예약 조회 메서드 추가
    public List<Appointment> getAppointmentsByMemberId(Long memberId) {
        // 회원 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        
        return appointmentRepository.findByMemberIdOrderByAppointmentDateDescAppointmentTimeDesc(memberId);
    }

    public List<Appointment> getTodayAppointmentsByMemberId(Long memberId) {
        // 회원 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByMemberIdAndAppointmentDate(memberId, today);
    }

    /**
     * 예약 상태를 내원 대기로 변경 (예약 시간 30분 전 등에 호출)
     * WAITING → BOOKED
     */
    @Transactional
    public void startWaitingForAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));
        
        if (appointment.getStatus() != AppointmentStatus.WAITING) {
            throw new BusinessException(ErrorCode.APPOINTMENT_INVALID_STATUS);
        }
        
        appointment.startWaiting();
        log.info("예약 대기 상태 변경: {} (예약 ID: {})", appointment.getMember().getName(), appointmentId);
    }

}