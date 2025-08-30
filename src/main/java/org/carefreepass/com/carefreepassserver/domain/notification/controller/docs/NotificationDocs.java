package org.carefreepass.com.carefreepassserver.domain.notification.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.request.DeviceTokenRegisterRequest;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.request.PatientCallRequest;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.response.NotificationHistoryResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Operation(
            summary = "알림 이력 조회",
            description = "예약 ID별 또는 전체 알림 전송 이력을 조회합니다. 성공/실패 여부와 전송 시간을 확인할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이력 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ApiResponseTemplate<List<NotificationHistoryResponse>> getNotificationHistory(
            @RequestParam(required = false) Long appointmentId);

    @Operation(
            summary = "특정 예약의 알림 이력 조회",
            description = "특정 예약 ID의 모든 알림 전송 이력을 최신순으로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이력 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
            }
    )
    ApiResponseTemplate<List<NotificationHistoryResponse>> getNotificationHistoryByAppointment(
            @PathVariable Long appointmentId);
}