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

/**
 * 병원 예약 엔티티
 * 환자의 병원 진료 예약 정보를 관리합니다.
 */
@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Appointment extends BaseTimeEntity {

    /** 예약 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 예약한 환자 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 병원명 */
    @Column(nullable = false, length = 100)
    private String hospitalName;

    /** 진료과 */
    @Column(nullable = false, length = 50)
    private String department;

    /** 예약 날짜 */
    @Column(nullable = false)
    private LocalDate appointmentDate;

    /** 예약 시간 */
    @Column(nullable = false)
    private LocalTime appointmentTime;

    /** 예약 상태 (BOOKED, ARRIVED, CALLED, COMPLETED, CANCELLED) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    /**
     * 예약 엔티티 생성자 (빌더 패턴)
     * 외부에서 직접 호출할 수 없으며, 정적 팩토리 메서드를 통해서만 생성 가능
     */
    @Builder(access = AccessLevel.PRIVATE)
    private Appointment(Member member, String hospitalName, String department,
                       LocalDate appointmentDate, LocalTime appointmentTime) {
        this.member = member;
        this.hospitalName = hospitalName;
        this.department = department;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = AppointmentStatus.BOOKED; // 초기 상태는 예약 완료
    }

    /**
     * 예약 생성 정적 팩토리 메서드
     * 새로운 예약을 생성하며, 초기 상태는 BOOKED로 설정됩니다.
     * 
     * @param member 예약한 환자
     * @param hospitalName 병원명
     * @param department 진료과
     * @param appointmentDate 예약 날짜
     * @param appointmentTime 예약 시간
     * @return 생성된 예약 엔티티
     */
    public static Appointment createAppointment(Member member, String hospitalName, String department,
                                              LocalDate appointmentDate, LocalTime appointmentTime) {
        return Appointment.builder()
                .member(member)
                .hospitalName(hospitalName)
                .department(department)
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .build();
    }

    /**
     * 예약 상태 변경
     * 
     * @param status 변경할 예약 상태
     */
    public void updateStatus(AppointmentStatus status) {
        this.status = status;
    }

    /**
     * 환자 체크인 처리
     * 예약 상태를 ARRIVED(도착)로 변경합니다.
     */
    public void checkin() {
        this.status = AppointmentStatus.ARRIVED;
    }

    /**
     * 환자 호출 처리
     * 예약 상태를 CALLED(호출됨)로 변경합니다.
     */
    public void call() {
        this.status = AppointmentStatus.CALLED;
    }

    /**
     * 환자 호출 가능 여부 확인
     * 완료되거나 취소된 예약은 호출할 수 없습니다.
     * 
     * @return 호출 가능 여부
     */
    public boolean canCall() {
        return this.status != AppointmentStatus.COMPLETED && this.status != AppointmentStatus.CANCELLED;
    }

    /**
     * 예약 정보 수정
     * 완료되거나 취소된 예약이 아닌 경우에만 수정 가능합니다.
     * 
     * @param hospitalName 변경할 병원명
     * @param department 변경할 진료과
     * @param appointmentDate 변경할 예약 날짜
     * @param appointmentTime 변경할 예약 시간
     */
    public void updateAppointment(String hospitalName, String department,
                                LocalDate appointmentDate, LocalTime appointmentTime) {
        this.hospitalName = hospitalName;
        this.department = department;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
    }
}