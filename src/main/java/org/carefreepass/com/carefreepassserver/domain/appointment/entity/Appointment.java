package org.carefreepass.com.carefreepassserver.domain.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Appointment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 100)
    private String hospitalName;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(length = 50)
    private String doctorName;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalTime appointmentTime;

    @Column(length = 20)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private Appointment(Member member, String hospitalName, String department, String doctorName,
                       LocalDate appointmentDate, LocalTime appointmentTime, String roomNumber) {
        this.member = member;
        this.hospitalName = hospitalName;
        this.department = department;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.roomNumber = roomNumber;
        this.status = AppointmentStatus.BOOKED;
    }

    public static Appointment createAppointment(Member member, String hospitalName, String department,
                                              String doctorName, LocalDate appointmentDate, LocalTime appointmentTime,
                                              String roomNumber) {
        return Appointment.builder()
                .member(member)
                .hospitalName(hospitalName)
                .department(department)
                .doctorName(doctorName)
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .roomNumber(roomNumber)
                .build();
    }

    public void updateStatus(AppointmentStatus status) {
        this.status = status;
    }

    public void checkin() {
        this.status = AppointmentStatus.ARRIVED;
    }

    public void call() {
        this.status = AppointmentStatus.CALLED;
    }

    public boolean canCall() {
        return this.status != AppointmentStatus.COMPLETED && this.status != AppointmentStatus.CANCELLED;
    }

    public void updateAppointment(String hospitalName, String department, String doctorName,
                                LocalDate appointmentDate, LocalTime appointmentTime, String roomNumber) {
        this.hospitalName = hospitalName;
        this.department = department;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.roomNumber = roomNumber;
    }
}