package org.carefreepass.com.carefreepassserver.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    private LocalDate birthDate;

    private Gender gender;

    @Column(nullable = false)
    private String password;

    @Builder(access = AccessLevel.PRIVATE)
    private PatientProfile(Member member, LocalDate birthDate, Gender gender, String password) {
        this.member = member;
        this.birthDate = birthDate;
        this.gender = gender;
        this.password = password;
    }
}