package org.carefreepass.com.carefreepassserver.domain.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

// 시간대 예외 관리 엔티티 - 특정 날짜와 시간에 대한 예약 차단 관리
@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlotException extends BaseTimeEntity {

    // 시간 예외 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 해당 진료과
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_department_id", nullable = false)
    private HospitalDepartment hospitalDepartment;

    // 예외 적용 날짜
    @Column(nullable = false)
    private LocalDate exceptionDate;

    // 예외 적용 시간
    @Column(nullable = false)
    private LocalTime exceptionTime;

    // 차단 여부 (true: 차단, false: 허용)
    @Column(nullable = false)
    private Boolean blocked;

    // 시간 예외 엔티티 생성자 (빌더 패턴)
    @Builder(access = AccessLevel.PRIVATE)
    private TimeSlotException(HospitalDepartment hospitalDepartment, LocalDate exceptionDate,
                             LocalTime exceptionTime, Boolean blocked) {
        this.hospitalDepartment = hospitalDepartment;
        this.exceptionDate = exceptionDate;
        this.exceptionTime = exceptionTime;
        this.blocked = blocked;
    }

    // 시간 차단 생성 정적 팩토리 메서드
    public static TimeSlotException createBlockedTimeSlot(HospitalDepartment hospitalDepartment,
                                                         LocalDate exceptionDate, LocalTime exceptionTime) {
        return TimeSlotException.builder()
                .hospitalDepartment(hospitalDepartment)
                .exceptionDate(exceptionDate)
                .exceptionTime(exceptionTime)
                .blocked(true)
                .build();
    }

    // 차단 여부 확인
    public boolean isBlocked() {
        return this.blocked;
    }

    // 차단 해제
    public void unblock() {
        this.blocked = false;
    }

    // 차단 설정
    public void block() {
        this.blocked = true;
    }
}