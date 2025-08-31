package org.carefreepass.com.carefreepassserver.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PatientCallRequest {

    @Schema(description = "예약 ID", example = "1")
    @NotNull(message = "예약 ID는 필수입니다.")
    private Long appointmentId;

    @Schema(description = "진료실 번호", example = "101호")
    private String roomNumber;
}