package org.carefreepass.com.carefreepassserver.domain.appointment.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.AppointmentStatus;
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 예약 리포지토리
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 회원의 특정 날짜 예약 존재 여부 확인 (상태별)
    boolean existsByMemberIdAndAppointmentDateAndStatus(Long memberId, LocalDate appointmentDate, AppointmentStatus status);

    // 특정 날짜의 특정 상태 예약 목록 조회
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member WHERE a.appointmentDate = :date AND a.status IN :statuses ORDER BY a.appointmentTime")
    List<Appointment> findTodayAppointmentsByStatus(@Param("date") LocalDate date, @Param("statuses") List<AppointmentStatus> statuses);

    // 특정 날짜의 모든 예약 목록 조회
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member WHERE a.appointmentDate = :date ORDER BY a.appointmentTime")
    List<Appointment> findAllByAppointmentDate(@Param("date") LocalDate date);


    // 병원 진료과별 예약된 시간 조회
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member WHERE a.hospitalDepartment = :department AND a.appointmentDate = :date AND a.status = :status ORDER BY a.appointmentTime")
    List<Appointment> findByHospitalDepartmentAndDateAndStatus(@Param("department") HospitalDepartment hospitalDepartment, @Param("date") LocalDate date, @Param("status") AppointmentStatus status);

    // 병원 진룝과와 특정 시간의 예약 존재 여부 확인
    boolean existsByHospitalDepartmentAndAppointmentDateAndAppointmentTimeAndStatus(
            HospitalDepartment hospitalDepartment, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);

    // 환자별 예약 목록 조회 (최신순 정렬)
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member m JOIN FETCH a.hospitalDepartment hd JOIN FETCH hd.hospital WHERE m.id = :memberId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByMemberIdOrderByAppointmentDateDescAppointmentTimeDesc(@Param("memberId") Long memberId);

    // 환자별 현재/미래 예약만 조회 (과거 예약 제외)
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member m JOIN FETCH a.hospitalDepartment hd JOIN FETCH hd.hospital WHERE m.id = :memberId AND a.appointmentDate >= :currentDate ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findByMemberIdAndAppointmentDateGreaterThanEqual(@Param("memberId") Long memberId, @Param("currentDate") LocalDate currentDate);

    // 환자의 특정 날짜 예약 조회
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member m JOIN FETCH a.hospitalDepartment hd JOIN FETCH hd.hospital WHERE m.id = :memberId AND a.appointmentDate = :date ORDER BY a.appointmentTime")
    List<Appointment> findByMemberIdAndAppointmentDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    // 병원 진료과의 특정 날짜 활성 예약 목록 조회
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member WHERE a.hospitalDepartment = :department AND a.appointmentDate = :date AND a.status IN :statuses ORDER BY a.appointmentTime")
    List<Appointment> findByHospitalDepartmentAndAppointmentDateAndStatusIn(@Param("department") HospitalDepartment hospitalDepartment, @Param("date") LocalDate date, @Param("statuses") List<AppointmentStatus> statuses);
}