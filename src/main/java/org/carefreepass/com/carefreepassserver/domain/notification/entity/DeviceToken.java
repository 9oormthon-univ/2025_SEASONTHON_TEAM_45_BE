package org.carefreepass.com.carefreepassserver.domain.notification.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;
import org.carefreepass.com.carefreepassserver.golbal.domain.Status;

@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 500)
    private String fcmToken;

    @Column(nullable = false, length = 20)
    private String deviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column
    private LocalDateTime lastUsedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private DeviceToken(Member member, String fcmToken, String deviceType) {
        this.member = member;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.status = Status.ACTIVE;
        this.lastUsedAt = LocalDateTime.now();
    }

    public static DeviceToken createDeviceToken(Member member, String fcmToken, String deviceType) {
        return DeviceToken.builder()
                .member(member)
                .fcmToken(fcmToken)
                .deviceType(deviceType)
                .build();
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.lastUsedAt = LocalDateTime.now();
        this.status = Status.ACTIVE;
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
    }

    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }
}