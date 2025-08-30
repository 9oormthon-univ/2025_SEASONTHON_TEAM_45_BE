package org.carefreepass.com.carefreepassserver.domain.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {

    private boolean isFirebaseAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }

    public boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (!isFirebaseAvailable()) {
            log.warn("Firebase not initialized. Cannot send FCM notification.");
            return false;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);

            return true;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token: {}", fcmToken, e);

            if ("INVALID_ARGUMENT".equals(e.getErrorCode()) ||
                "UNREGISTERED".equals(e.getErrorCode())) {
                log.warn("Invalid FCM token detected: {}", fcmToken);
            }

            return false;
        } catch (Exception e) {
            log.error("Unexpected error while sending FCM message", e);
            return false;
        }
    }

    public boolean sendCallNotification(String fcmToken, String patientName, String roomNumber) {
        String title = "진료 호출";
        String body = String.format("%s님, %s로 들어오세요.", patientName, roomNumber);

        Map<String, String> data = new HashMap<>();
        data.put("type", "PATIENT_CALL");
        data.put("patient_name", patientName);
        data.put("room_number", roomNumber);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return sendNotification(fcmToken, title, body, data);
    }

    public boolean sendAppointmentConfirmation(String fcmToken, String patientName, String hospitalName, String appointmentDate, String appointmentTime) {
        String title = "예약 확인";
        String body = String.format("%s님, %s %s %s 예약이 완료되었습니다.", patientName, hospitalName, appointmentDate, appointmentTime);

        Map<String, String> data = new HashMap<>();
        data.put("type", "APPOINTMENT_CONFIRMATION");
        data.put("patient_name", patientName);
        data.put("hospital_name", hospitalName);
        data.put("appointment_date", appointmentDate);
        data.put("appointment_time", appointmentTime);

        return sendNotification(fcmToken, title, body, data);
    }
}