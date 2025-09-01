package org.carefreepass.com.carefreepassserver.domain.appointment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AvailableTimeSlotsResponse {

    @Schema(description = "조회 날짜", example = "2025-09-04")
    private LocalDate date;

    @Schema(description = "진료과명", example = "내과")
    private String departmentName;

    @Schema(description = "시간대 목록")
    private List<TimeSlotResponse> timeSlots;

    @Schema(description = "예약 가능한 시간 개수", example = "11")
    private Integer availableCount;

    @Schema(description = "전체 시간대 개수", example = "14")
    private Integer totalSlots;

    public static AvailableTimeSlotsResponse of(LocalDate date, String departmentName, 
                                               List<TimeSlotResponse> timeSlots) {
        int availableCount = (int) timeSlots.stream()
                .filter(TimeSlotResponse::getAvailable)
                .count();
        
        return new AvailableTimeSlotsResponse(
                date, departmentName, timeSlots, availableCount, timeSlots.size());
    }
}