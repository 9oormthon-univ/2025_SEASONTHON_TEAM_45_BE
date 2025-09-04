package org.carefreepass.com.carefreepassserver.domain.appointment.entity;

public enum AppointmentStatus {
    WAITING("예약 확정"),
    SCHEDULED("오늘 내원전"),
    ARRIVED("병원 도착"),
    CALLED("진료실 호출"),
    COMPLETED("진료 완료"),
    CANCELLED("예약 취소");

    private final String description;

    AppointmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}