package org.carefreepass.com.carefreepassserver.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String name;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(MemberRole role, Status status, String name, String phoneNumber, String password) {
        this.role = role;
        this.status = status;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public static Member createMember(MemberRole role, String name, String phoneNumber, String password) {
        return Member.builder()
                .role(role)
                .status(Status.ACTIVE)
                .name(name)
                .phoneNumber(phoneNumber)
                .password(password)
                .build();
    }
}
