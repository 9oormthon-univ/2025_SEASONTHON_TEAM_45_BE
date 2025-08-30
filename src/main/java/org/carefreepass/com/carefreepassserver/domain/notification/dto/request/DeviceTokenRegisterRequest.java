package org.carefreepass.com.carefreepassserver.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeviceTokenRegisterRequest {

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String fcmToken;

    @NotBlank(message = "기기 타입은 필수입니다.")
    private String deviceType;
}