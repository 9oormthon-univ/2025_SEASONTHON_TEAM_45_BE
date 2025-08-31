package org.carefreepass.com.carefreepassserver.domain.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PatientCallRequest {

    @NotNull(message = "예약 ID는 필수입니다.")
    private Long appointmentId;

    private String roomNumber;
}