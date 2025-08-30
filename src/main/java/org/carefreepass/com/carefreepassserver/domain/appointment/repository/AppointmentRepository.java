package org.carefreepass.com.carefreepassserver.domain.appointment.repository;

import java.time.LocalDate;
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
}