package org.carefreepass.com.carefreepassserver.domain.notification.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.request.DeviceTokenRegisterRequest;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.request.PatientCallRequest;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "알림 API", description = "FCM 푸시 알림 전용 API")
public interface NotificationDocs {

    @Operation(
            summary = "FCM 토큰 등록",
            description = "환자 앱에서 FCM 토큰을 서버에 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    ApiResponseTemplate<String> registerDeviceToken(@Valid @RequestBody DeviceTokenRegisterRequest request);

    @Operation(
            summary = "환자 호출",
            description = "웹 관리자가 도착한 환자를 호출합니다. (핵심 기능)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "호출 성공"),
                    @ApiResponse(responseCode = "400", description = "호출 불가능한 상태"),
                    @ApiResponse(responseCode = "500", description = "푸시 알림 전송 실패")
            }
    )
    ApiResponseTemplate<String> callPatient(@Valid @RequestBody PatientCallRequest request);
}