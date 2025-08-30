package org.carefreepass.com.carefreepassserver.domain.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.AppointmentService;
import org.carefreepass.com.carefreepassserver.domain.notification.controller.docs.NotificationDocs;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.request.DeviceTokenRegisterRequest;
import org.carefreepass.com.carefreepassserver.domain.notification.dto.request.PatientCallRequest;
import org.carefreepass.com.carefreepassserver.domain.notification.service.NotificationService;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
                    .code("NOTIFICATION_2001")
                    .message("FCM 토큰이 성공적으로 등록되었습니다.")
                    .body("SUCCESS");

        } catch (IllegalArgumentException e) {
            return ApiResponseTemplate.error()
                    .code("NOTIFICATION_4001")
                    .message(e.getMessage())
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
                    .code("NOTIFICATION_2004")
                    .message("환자 호출이 완료되었습니다.")
                    .body("SUCCESS");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponseTemplate.error()
                    .code("NOTIFICATION_4004")
                    .message(e.getMessage())
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

}