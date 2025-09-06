package org.carefreepass.com.carefreepassserver.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Builder(access = AccessLevel.PRIVATE)
    private PatientProfile(Member member, String birthDate, Gender gender) {
        this.member = member;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static PatientProfile createPatientProfile(Member member, String birthDate, Gender gender) {
        return PatientProfile.builder()
                .member(member)
                .birthDate(birthDate)
                .gender(gender)
                .build();
    }
}
