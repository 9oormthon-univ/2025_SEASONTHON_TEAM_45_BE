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
                AppointmentStatus.SCHEDULED,
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

        log.info("예약 생성 완료: 회원 {} (ID: {}), 진료과: {}", 
                member.getName(), request.getMemberId(), request.getDepartmentName());
        return savedAppointment.getId();
    }

    /**
     * 환자 체크인을 처리합니다.
     * SCHEDULED 상태의 예약을 ARRIVED 상태로 변경합니다.
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

        // 체크인 가능한 상태인지 확인 (SCHEDULED 상태에서만 체크인 가능)
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CANNOT_MODIFY_COMPLETED);
        }

        // 체크인 처리 (상태를 ARRIVED로 변경)
        appointment.checkin();
        log.info("환자 체크인 완료: {} (예약 ID: {})", appointment.getMember().getName(), appointmentId);
    }

    public List<Appointment> getTodayWaitingPatients() {
        List<AppointmentStatus> waitingStatuses = Arrays.asList(
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.ARRIVED,
                AppointmentStatus.CALLED
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
     * 환자를 호출합니다. (폴링 기반)
     * 예약 상태를 CALLED로 변경하여 환자 앱에서 폴링으로 감지할 수 있도록 합니다.
     * 
     * @param appointmentId 예약 ID
     * @throws BusinessException 존재하지 않는 예약인 경우 (APPOINTMENT_NOT_FOUND)
     * @throws BusinessException 호출 불가능한 상태인 경우 (APPOINTMENT_CALL_NOT_AVAILABLE)
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

        // 예약 상태를 CALLED로 변경 (폴링으로 감지됨)
        appointment.call();
        log.info("환자 호출 완료: {} (예약 ID: {})", 
                appointment.getMember().getName(), appointmentId);
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
     * WAITING → SCHEDULED
     */
    @Transactional
    public void startWaitingForAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND));
        
        if (appointment.getStatus() != AppointmentStatus.WAITING) {
            throw new BusinessException(ErrorCode.APPOINTMENT_INVALID_STATUS);
        }
        
        appointment.scheduleForToday();
        log.info("예약 대기 상태 변경: {} (예약 ID: {})", appointment.getMember().getName(), appointmentId);
    }

    /**
     * 특정 날짜의 모든 예약 조회 (관리자용)
     * 
     * @param date 조회할 날짜
     * @return 해당 날짜의 모든 예약 목록
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findAllByAppointmentDate(date);
    }

    /**
     * 오늘 날짜의 WAITING 상태 예약을 SCHEDULED로 변경 (스케줄러용)
     * 
     * @return 업데이트된 예약 개수
     */
    @Transactional
    public int updateTodayWaitingToScheduled() {
        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentRepository.findAllByAppointmentDate(today);
        
        int updatedCount = 0;
        for (Appointment appointment : todayAppointments) {
            if (appointment.getStatus() == AppointmentStatus.WAITING) {
                appointment.updateStatus(AppointmentStatus.SCHEDULED);
                updatedCount++;
                log.info("예약 상태 변경: {} (ID: {}) - WAITING → SCHEDULED", 
                        appointment.getMember().getName(), appointment.getId());
            }
        }
        
        log.info("오늘 날짜({}) 예약 상태 업데이트 완료 - 총 {}건", today, updatedCount);
        return updatedCount;
    }

}