package org.carefreepass.com.carefreepassserver.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.golbal.domain.BaseTimeEntity;

@Entity
@Getter
@EqualsAndHashCode(callSuper = false, of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Boolean isSuccess;

    @Column(length = 500)
    private String errorMessage;

    @Builder(access = AccessLevel.PRIVATE)
    private NotificationHistory(Appointment appointment, String title, String message, Boolean isSuccess, String errorMessage) {
        this.appointment = appointment;
        this.title = title;
        this.message = message;
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }

    public static NotificationHistory createSuccess(Appointment appointment, String title, String message) {
        return NotificationHistory.builder()
                .appointment(appointment)
                .title(title)
                .message(message)
                .isSuccess(true)
                .build();
    }

    public static NotificationHistory createFailure(Appointment appointment, String title, String message, String errorMessage) {
        return NotificationHistory.builder()
                .appointment(appointment)
                .title(title)
                .message(message)
                .isSuccess(false)
                .errorMessage(errorMessage)
                .build();
    }
}