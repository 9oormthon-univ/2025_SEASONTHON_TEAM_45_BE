package org.carefreepass.com.carefreepassserver.domain.appointment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AppointmentCheckinRequest {

    @NotNull(message = "예약 ID는 필수입니다.")
    private Long appointmentId;

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;
}