package org.carefreepass.com.carefreepassserver.domain.appointment.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
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
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.PatientProfile;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.domain.auth.repository.PatientProfileRepository;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.AppointmentResponse;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 예약 관리 서비스
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AppointmentService {
    
    private static final List<AppointmentStatus> ACTIVE_STATUSES = List.of(
            AppointmentStatus.WAITING,
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.ARRIVED,
            AppointmentStatus.CALLED
    );
    
    private static final List<AppointmentStatus> WAITING_STATUSES = List.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.ARRIVED,
            AppointmentStatus.CALLED
    );

    private final AppointmentRepository appointmentRepository;
    private final MemberRepository memberRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalDepartmentRepository hospitalDepartmentRepository;
    private final PatientProfileRepository patientProfileRepository;

    // 새로운 예약 생성
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
        
        for (AppointmentStatus status : ACTIVE_STATUSES) {
            if (appointmentRepository.existsByMemberIdAndAppointmentDateAndStatus(
                    request.getMemberId(), request.getAppointmentDate(), status)) {
                throw new BusinessException(ErrorCode.APPOINTMENT_DUPLICATE_DATE);
            }
        }

        // 2. 진료과별 시간 충돌 검사 (같은 진료과, 같은 시간에 활성 예약이 있는지 확인)
        for (AppointmentStatus status : ACTIVE_STATUSES) {
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

    // 환자 체크인 처리
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
        return appointmentRepository.findTodayAppointmentsByStatus(LocalDate.now(), WAITING_STATUSES);
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

    // 환자 호출 (폴링 기반)
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

    // 환자용 예약 조회 메서드 (과거 예약 제외)
    public List<Appointment> getAppointmentsByMemberId(Long memberId) {
        // 회원 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        
        // 현재 날짜 이후의 예약만 조회 (과거 예약 제외)
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByMemberIdAndAppointmentDateGreaterThanEqual(memberId, today);
    }

    public List<Appointment> getTodayAppointmentsByMemberId(Long memberId) {
        // 회원 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByMemberIdAndAppointmentDate(memberId, today);
    }

    // 예약 상태를 내원 대기로 변경 (WAITING → SCHEDULED)
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

    // 특정 날짜의 모든 예약 조회 (관리자용)
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findAllByAppointmentDate(date);
    }

    // 오늘 날짜 WAITING 예약을 SCHEDULED로 변경 (스케줄러용)
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

    // Appointment를 AppointmentResponse로 변환하는 유틸리티 메서드
    public AppointmentResponse convertToResponse(Appointment appointment) {
        PatientProfile patientProfile = patientProfileRepository.findByMember(appointment.getMember()).orElse(null);
        return AppointmentResponse.from(appointment, patientProfile);
    }

    // 여러 Appointment를 AppointmentResponse 목록으로 변환하는 유틸리티 메서드
    public List<AppointmentResponse> convertToResponseList(List<Appointment> appointments) {
        return appointments.stream()
                .map(this::convertToResponse)
                .toList();
    }

}