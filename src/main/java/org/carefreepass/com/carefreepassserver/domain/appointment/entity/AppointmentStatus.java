package org.carefreepass.com.carefreepassserver.domain.appointment.entity;

public enum AppointmentStatus {
    BOOKED("예약됨"),
    WAITING_BEFORE_ARRIVAL("내원전"),
    ARRIVED("대기중"),
    CALLED("호출됨"),
    COMPLETED("완료됨"),
    CANCELLED("취소됨");

    private final String description;

    AppointmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}