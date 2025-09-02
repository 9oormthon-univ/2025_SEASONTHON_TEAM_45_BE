package org.carefreepass.com.carefreepassserver.domain.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    private String address;

    @Builder
    private Hospital(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public static Hospital createHospital(String name, String address) {
        return Hospital.builder()
                .name(name)
                .address(address)
                .build();
    }
}

