package org.carefreepass.com.carefreepassserver.domain.appointment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TimeSlotResponse {

    @Schema(description = "시간", example = "10:00")
    private LocalTime time;

    @Schema(description = "예약 가능 여부", example = "true")
    private Boolean available;

    @Schema(description = "불가능한 이유", example = "ALREADY_BOOKED")
    private String reason;

    @Schema(description = "예약자명 (이미 예약된 경우)", example = "김환자")
    private String bookedBy;

    public static TimeSlotResponse available(LocalTime time) {
        return new TimeSlotResponse(time, true, null, null);
    }

    public static TimeSlotResponse alreadyBooked(LocalTime time, String bookedBy) {
        return new TimeSlotResponse(time, false, "이미 예약됨", bookedBy);
    }

    public static TimeSlotResponse hospitalBlocked(LocalTime time) {
        return new TimeSlotResponse(time, false, "이미 예약됨", null);
    }
}