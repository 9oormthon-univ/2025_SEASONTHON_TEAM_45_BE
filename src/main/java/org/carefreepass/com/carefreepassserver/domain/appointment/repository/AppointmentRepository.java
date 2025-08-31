package org.carefreepass.com.carefreepassserver.domain.appointment.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 예약 리포지토리
 * 병원 예약 정보에 대한 데이터베이스 접근을 담당합니다.
 * 예약 생성, 조회, 중복 검사, 시간 충돌 검사 등의 기능을 제공합니다.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * 회원의 특정 날짜 예약 존재 여부 확인 (상태별)
     * 같은 날짜에 이미 예약이 있는지 확인하여 중복 예약을 방지합니다.
     * 
     * @param memberId 회원 ID
     * @param appointmentDate 예약 날짜
     * @param status 확인할 예약 상태
     * @return 예약 존재 여부
     */
    boolean existsByMemberIdAndAppointmentDateAndStatus(Long memberId, LocalDate appointmentDate, AppointmentStatus status);

    /**
     * 특정 날짜의 특정 상태 예약 목록 조회 (회원 정보 포함)
     * 오늘 날짜의 대기 중인 환자 목록을 조회할 때 주로 사용됩니다.
     * 
     * @param date 조회할 날짜
     * @param statuses 조회할 예약 상태 목록
     * @return 예약 시간 순으로 정렬된 예약 목록
     */
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member WHERE a.appointmentDate = :date AND a.status IN :statuses ORDER BY a.appointmentTime")
    List<Appointment> findTodayAppointmentsByStatus(@Param("date") LocalDate date, @Param("statuses") List<AppointmentStatus> statuses);

    /**
     * 특정 날짜의 모든 예약 목록 조회 (회원 정보 포함)
     * 하루 전체 예약 현황을 파악할 때 사용됩니다.
     * 
     * @param date 조회할 날짜
     * @return 예약 시간 순으로 정렬된 모든 예약 목록
     */
    @Query("SELECT a FROM Appointment a JOIN FETCH a.member WHERE a.appointmentDate = :date ORDER BY a.appointmentTime")
    List<Appointment> findAllByAppointmentDate(@Param("date") LocalDate date);

    /**
     * 진료과별 시간 충돌 검사
     * 같은 진료과, 같은 날짜, 같은 시간에 이미 예약이 있는지 확인합니다.
     * 
     * @param department 진료과명
     * @param appointmentDate 예약 날짜
     * @param appointmentTime 예약 시간
     * @param status 확인할 예약 상태
     * @return 충돌 여부 (true: 충돌 있음, false: 예약 가능)
     */
    boolean existsByDepartmentAndAppointmentDateAndAppointmentTimeAndStatus(
            String department, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);

    /**
     * 진료과별 예약된 시간 조회
     * 특정 진료과의 특정 날짜에 이미 예약된 시간들을 조회하여 
     * 예약 가능한 시간을 계산할 때 사용됩니다.
     * 
     * @param department 진료과명
     * @param date 조회할 날짜
     * @param status 조회할 예약 상태
     * @return 시간 순으로 정렬된 예약된 시간 목록
     */
    @Query("SELECT a.appointmentTime FROM Appointment a WHERE a.department = :department AND a.appointmentDate = :date AND a.status = :status ORDER BY a.appointmentTime")
    List<LocalTime> findBookedTimesByDepartmentAndDate(@Param("department") String department, @Param("date") LocalDate date, @Param("status") AppointmentStatus status);
}