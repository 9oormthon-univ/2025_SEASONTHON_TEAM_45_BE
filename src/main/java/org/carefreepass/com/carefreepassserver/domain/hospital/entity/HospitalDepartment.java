package org.carefreepass.com.carefreepassserver.domain.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

/**
 * 병원별 진료과 엔티티
 * 각 병원이 운영하는 진료과 정보를 관리합니다.
 */
@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HospitalDepartment extends BaseTimeEntity {

    /** 진료과 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소속 병원 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    /** 진료과명 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 진료과 설명 */
    @Column(length = 200)
    private String description;

    /** 기본 진료 시작 시간 */
    @Column(nullable = false)
    private LocalTime defaultStartTime;

    /** 기본 진료 종료 시간 */
    @Column(nullable = false)
    private LocalTime defaultEndTime;

    /** 예약 슬롯 간격(분) */
    @Column(nullable = false)
    private Integer slotDurationMinutes;

    /** 진료과 활성화 상태 */
    @Column(nullable = false)
    private Boolean active;

    /**
     * 진료과 엔티티 생성자 (빌더 패턴)
     */
    @Builder(access = AccessLevel.PRIVATE)
    private HospitalDepartment(Hospital hospital, String name, String description,
                              LocalTime defaultStartTime, LocalTime defaultEndTime,
                              Integer slotDurationMinutes) {
        this.hospital = hospital;
        this.name = name;
        this.description = description;
        this.defaultStartTime = defaultStartTime;
        this.defaultEndTime = defaultEndTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.active = true;
    }

    /**
     * 진료과 생성 정적 팩토리 메서드
     * 
     * @param hospital 소속 병원
     * @param name 진료과명
     * @param description 진료과 설명
     * @return 생성된 진료과 엔티티
     */
    public static HospitalDepartment createDepartment(Hospital hospital, String name, String description) {
        return HospitalDepartment.builder()
                .hospital(hospital)
                .name(name)
                .description(description)
                .defaultStartTime(LocalTime.of(10, 0))
                .defaultEndTime(LocalTime.of(16, 30))
                .slotDurationMinutes(30)
                .build();
    }

    /**
     * 진료과 정보 수정
     * 
     * @param name 변경할 진료과명
     * @param description 변경할 진료과 설명
     */
    public void updateDepartment(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 진료과 비활성화
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * 진료과 활성화
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 진료과 활성화 여부 확인
     * 
     * @return 활성화 상태
     */
    public boolean isActive() {
        return this.active;
    }
}