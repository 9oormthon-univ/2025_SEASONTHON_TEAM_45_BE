package org.carefreepass.com.carefreepassserver.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeviceTokenRegisterRequest {

    @Schema(description = "회원 ID", example = "1")
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @Schema(description = "FCM 디바이스 토큰", example = "dGhpc2lzYXNhbXBsZWZjbXRva2Vu...")
    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String fcmToken;

    @Schema(description = "디바이스 타입 (ANDROID/IOS)", example = "ANDROID")
    @NotBlank(message = "기기 타입은 필수입니다.")
    @Pattern(regexp = "^(ANDROID|IOS)$", message = "기기 타입은 ANDROID, IOS 중 하나여야 합니다")
    private String deviceType;
}