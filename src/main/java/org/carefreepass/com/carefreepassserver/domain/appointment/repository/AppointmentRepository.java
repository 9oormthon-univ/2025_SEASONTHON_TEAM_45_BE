package org.carefreepass.com.carefreepassserver.domain.appointment.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByMemberIdAndAppointmentDateAndStatus(Long memberId, LocalDate appointmentDate, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.member WHERE a.appointmentDate = :date AND a.status IN :statuses ORDER BY a.appointmentTime")
    List<Appointment> findTodayAppointmentsByStatus(@Param("date") LocalDate date, @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.member WHERE a.appointmentDate = :date ORDER BY a.appointmentTime")
    List<Appointment> findAllByAppointmentDate(@Param("date") LocalDate date);

    // 같은 시간대 충돌 검사 (의사별)
    boolean existsByDoctorNameAndAppointmentDateAndAppointmentTimeAndStatus(
            String doctorName, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);

    // 같은 시간대 충돌 검사 (진료실별)  
    boolean existsByRoomNumberAndAppointmentDateAndAppointmentTimeAndStatus(
            String roomNumber, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status);

    // 예약 가능 시간 조회 (의사별)
    @Query("SELECT a.appointmentTime FROM Appointment a WHERE a.doctorName = :doctorName AND a.appointmentDate = :date AND a.status = :status ORDER BY a.appointmentTime")
    List<LocalTime> findBookedTimesByDoctorAndDate(@Param("doctorName") String doctorName, @Param("date") LocalDate date, @Param("status") AppointmentStatus status);

    // 예약 가능 시간 조회 (진료실별)
    @Query("SELECT a.appointmentTime FROM Appointment a WHERE a.roomNumber = :roomNumber AND a.appointmentDate = :date AND a.status = :status ORDER BY a.appointmentTime")
    List<LocalTime> findBookedTimesByRoomAndDate(@Param("roomNumber") String roomNumber, @Param("date") LocalDate date, @Param("status") AppointmentStatus status);
}