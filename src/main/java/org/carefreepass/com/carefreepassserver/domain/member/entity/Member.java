package org.carefreepass.com.carefreepassserver.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;
import org.carefreepass.com.carefreepassserver.golbal.domain.Status;

@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@org.hibernate.annotations.DynamicUpdate
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String name;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "email", unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(MemberRole role, Status status,
                   String name, String phoneNumber,
                   String email, String password, LocalDate birthDate) {
        this.role = role;
        this.status = status;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
    }

    public static Member createPatient(String name, String phoneNumber, String password) {
        return Member.builder()
                .role(MemberRole.USER)
                .status(Status.ACTIVE)
                .name(name)
                .phoneNumber(phoneNumber)
                .password(password)
                .birthDate(null) // 기존 호환성을 위해 null로 설정
                .build();
    }

    public static Member createPatient(String name, String phoneNumber, String password, LocalDate birthDate) {
        return Member.builder()
                .role(MemberRole.USER)
                .status(Status.ACTIVE)
                .name(name)
                .phoneNumber(phoneNumber)
                .password(password)
                .birthDate(birthDate)
                .build();
    }

    // 병원 관리자용 생성자
    public static Member createHospitalAdmin(String name, String email, String password) {
        return Member.builder()
                .role(MemberRole.HOSPITAL)
                .status(Status.ACTIVE)
                .name(name)
                .email(email)
                .password(password)
                .birthDate(null)
                .build();
    }
}
