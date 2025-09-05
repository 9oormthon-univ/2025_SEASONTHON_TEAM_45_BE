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
import org.carefreepass.com.carefreepassserver.domain.hospital.entity.HospitalDepartment;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

// 병원 예약 엔티티 - 환자의 병원 진료 예약 정보 관리
@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Appointment extends BaseTimeEntity {

    // 예약 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약한 환자 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 예약 진료과
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_department_id", nullable = false)
    private HospitalDepartment hospitalDepartment;

    // 예약 날짜
    @Column(nullable = false)
    private LocalDate appointmentDate;

    // 예약 시간
    @Column(nullable = false)
    private LocalTime appointmentTime;

    // 예약 상태 (WAITING, SCHEDULED, ARRIVED, CALLED, COMPLETED, CANCELLED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    // 예약 엔티티 생성자 (빌더 패턴) - 외부에서 직접 호출 불가, 정적 팩토리 메서드 통해서만 생성
    @Builder(access = AccessLevel.PRIVATE)
    private Appointment(Member member, HospitalDepartment hospitalDepartment,
                       LocalDate appointmentDate, LocalTime appointmentTime, AppointmentStatus status) {
        this.member = member;
        this.hospitalDepartment = hospitalDepartment;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // 예약 생성 - 새로운 예약 생성 및 날짜에 따른 초기 상태 설정 (오늘:SCHEDULED, 미래:WAITING)
    public static Appointment createAppointment(Member member, HospitalDepartment hospitalDepartment,
                                              LocalDate appointmentDate, LocalTime appointmentTime) {
        // 오늘 날짜면 SCHEDULED, 미래 날짜면 WAITING
        AppointmentStatus initialStatus = appointmentDate.equals(LocalDate.now()) 
                ? AppointmentStatus.SCHEDULED 
                : AppointmentStatus.WAITING;
                
        return Appointment.builder()
                .member(member)
                .hospitalDepartment(hospitalDepartment)
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .status(initialStatus)
                .build();
    }

    // 예약 상태 변경
    public void updateStatus(AppointmentStatus status) {
        this.status = status;
    }

    // 환자 체크인 처리 - 예약 상태를 ARRIVED(도착)로 변경
    public void checkin() {
        this.status = AppointmentStatus.ARRIVED;
    }

    // 오늘 내원 예정 상태 변경 - WAITING에서 SCHEDULED로 변경
    public void scheduleForToday() {
        this.status = AppointmentStatus.SCHEDULED;
    }

    // 환자 호출 처리 - 예약 상태를 CALLED(호출됨)로 변경
    public void call() {
        this.status = AppointmentStatus.CALLED;
    }

    // 환자 호출 가능 여부 확인 (완료되거나 취소된 예약은 호출 불가)
    public boolean canCall() {
        return this.status != AppointmentStatus.COMPLETED && this.status != AppointmentStatus.CANCELLED;
    }

    // 예약 정보 수정 (완료되거나 취소된 예약은 수정 불가)
    public void updateAppointment(HospitalDepartment hospitalDepartment,
                                LocalDate appointmentDate, LocalTime appointmentTime) {
        this.hospitalDepartment = hospitalDepartment;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
    }

    // 병원명 조회 (편의 메서드)
    public String getHospitalName() {
        return this.hospitalDepartment.getHospital().getName();
    }

    // 진료과명 조회 (편의 메서드)
    public String getDepartmentName() {
        return this.hospitalDepartment.getName();
    }
}