package org.carefreepass.com.carefreepassserver.domain.notification.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.AppointmentService;
import org.carefreepass.com.carefreepassserver.domain.notification.controller.docs.NotificationDocs;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.request.DeviceTokenRegisterRequest;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.request.PatientCallRequest;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.response.NotificationHistoryResponse;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.NotificationHistory;
import org.carefreepass.com.carefreepassserver.domain.notification.service.NotificationService;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController implements NotificationDocs {

    private final NotificationService notificationService;
    private final AppointmentService appointmentService;

    @Override
    @PostMapping("/token")
    public ApiResponseTemplate<String> registerDeviceToken(@Valid @RequestBody DeviceTokenRegisterRequest request) {
        try {
            notificationService.registerDeviceToken(request.getMemberId(), request.getFcmToken(), request.getDeviceType());

            return ApiResponseTemplate.ok()
                    .body("SUCCESS");

        } catch (BusinessException e) {
            return ApiResponseTemplate.error()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to register FCM token", e);
            return ApiResponseTemplate.error()
                    .code("NOTIFICATION_5001")
                    .message("FCM 토큰 등록에 실패했습니다.")
                    .build();
        }
    }


    @Override
    @PostMapping("/call")
    public ApiResponseTemplate<String> callPatient(@Valid @RequestBody PatientCallRequest request) {
        try {
            appointmentService.callPatient(request.getAppointmentId(), request.getRoomNumber());

            return ApiResponseTemplate.ok()
                    .body("SUCCESS");

        } catch (BusinessException e) {
            return ApiResponseTemplate.error()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (RuntimeException e) {
            return ApiResponseTemplate.error()
                    .code("NOTIFICATION_5004")
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to call patient", e);
            return ApiResponseTemplate.error()
                    .code("NOTIFICATION_5005")
                    .message("환자 호출에 실패했습니다.")
                    .build();
        }
    }

    @Override
    @GetMapping("/history")
    public ApiResponseTemplate<List<NotificationHistoryResponse>> getNotificationHistory(
            @RequestParam(required = false) Long appointmentId) {
        try {
            List<NotificationHistory> histories = notificationService.getAllNotificationHistory(appointmentId);
            List<NotificationHistoryResponse> responses = histories.stream()
                    .map(NotificationHistoryResponse::from)
                    .toList();

            return ApiResponseTemplate.ok()
                    .body(responses);

        } catch (Exception e) {
            log.error("Failed to get notification history", e);
            return ApiResponseTemplate.error()
                    .code("NOTIFICATION_5006")
                    .message("알림 이력 조회에 실패했습니다.")
                    .build();
        }
    }

    @Override
    @GetMapping("/history/appointment/{appointmentId}")
    public ApiResponseTemplate<List<NotificationHistoryResponse>> getNotificationHistoryByAppointment(
            @PathVariable Long appointmentId) {
        try {
            List<NotificationHistory> histories = notificationService.getNotificationHistory(appointmentId);
            List<NotificationHistoryResponse> responses = histories.stream()
                    .map(NotificationHistoryResponse::from)
                    .toList();

            return ApiResponseTemplate.ok()
                    .body(responses);

        } catch (Exception e) {
            log.error("Failed to get notification history for appointment: {}", appointmentId, e);
            return ApiResponseTemplate.error()
                    .code("NOTIFICATION_5007")
                    .message("예약별 알림 이력 조회에 실패했습니다.")
                    .build();
        }
    }

}