package org.carefreepass.com.carefreepassserver.domain.hospital.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TimeSlotBlockRequest {

    @Schema(description = "진료과 ID", example = "1")
    @NotNull(message = "진료과 ID는 필수입니다.")
    private Long departmentId;

    @Schema(description = "차단할 날짜", example = "2025-09-04")
    @NotNull(message = "차단할 날짜는 필수입니다.")
    private LocalDate blockDate;

    @Schema(description = "차단할 시간", example = "14:00")
    @NotNull(message = "차단할 시간은 필수입니다.")
    private LocalTime blockTime;
}