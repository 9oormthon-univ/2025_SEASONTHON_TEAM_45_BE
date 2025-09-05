package org.carefreepass.com.carefreepassserver.domain.hospital.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.auth.entity.Member;

@Entity
@Getter
@Table(
  uniqueConstraints = @UniqueConstraint(columnNames = {"hospital_id","member_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HospitalMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="hospital_id")
    private Hospital hospital;

    @ManyToOne(optional=false) @JoinColumn(name="member_id")
    private Member member;

    private String email;

    @Builder(access = AccessLevel.PRIVATE)
    private HospitalMember(Hospital hospital, Member member, String email) {
        this.hospital = hospital;
        this.member = member;
        this.email = email;
    }

    public static HospitalMember createHospitalMember(Hospital hospital, Member member, String email) {
        return HospitalMember.builder()
                .hospital(hospital)
                .member(member)
                .email(email)
                .build();
    }
}
