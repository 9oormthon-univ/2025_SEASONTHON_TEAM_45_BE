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

/**
 * FCM 서비스
 * Firebase Cloud Messaging을 통한 푸시 알림 전송을 담당합니다.
 * 환자 호출, 예약 확인 등의 알림을 전송합니다.
 */
@Service
@Slf4j
public class FcmService {

    /**
     * Firebase 초기화 여부를 확인합니다.
     * @return Firebase 앱이 초기화되었으면 true, 그렇지 않으면 false
     */
    private boolean isFirebaseAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /**
     * 범용 FCM 알림을 전송합니다.
     * 
     * @param fcmToken 대상 디바이스의 FCM 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (Optional)
     * @return 전송 성공 여부
     */
    public boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        // Firebase 초기화 상태 확인
        if (!isFirebaseAvailable()) {
            log.warn("Firebase not initialized. Cannot send FCM notification.");
            return false;
        }

        try {
            // FCM 메시지 빌더 생성
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            // 추가 데이터가 있는 경우 포함
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // 메시지 전송
            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);

            return true;

        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송 실패, 토큰: {}", fcmToken, e);

            // 유효하지 않은 토큰 감지
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

    /**
     * 환자 호출 알림을 전송합니다. (핵심 기능)
     * "진료 호출" 제목으로 환자에게 진료실 입장 요청 알림을 보냅니다.
     * 
     * @param fcmToken 환자의 FCM 토큰
     * @param patientName 환자 이름
     * @param roomNumber 진료실 번호
     * @return 전송 성공 여부
     */
    public boolean sendCallNotification(String fcmToken, String patientName, String roomNumber) {
        // 알림 메시지 구성
        String title = "진료 호출";
        String body = String.format("%s님, %s로 들어오세요.", patientName, roomNumber);

        // 추가 데이터 구성 (앱에서 추가 처리용)
        Map<String, String> data = new HashMap<>();
        data.put("type", "PATIENT_CALL");
        data.put("patient_name", patientName);
        data.put("room_number", roomNumber);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return sendNotification(fcmToken, title, body, data);
    }

    /**
     * 예약 확인 알림을 전송합니다.
     * 예약 생성 시 자동으로 발송되는 확인 알림입니다.
     * 
     * @param fcmToken 환자의 FCM 토큰
     * @param patientName 환자 이름
     * @param hospitalName 병원명
     * @param appointmentDate 예약 날짜
     * @param appointmentTime 예약 시간
     * @return 전송 성공 여부
     */
    public boolean sendAppointmentConfirmation(String fcmToken, String patientName, String hospitalName, String appointmentDate, String appointmentTime) {
        // 알림 메시지 구성
        String title = "예약 확인";
        String body = String.format("%s님, %s %s %s 예약이 완료되었습니다.", patientName, hospitalName, appointmentDate, appointmentTime);

        // 추가 데이터 구성 (앱에서 추가 처리용)
        Map<String, String> data = new HashMap<>();
        data.put("type", "APPOINTMENT_CONFIRMATION");
        data.put("patient_name", patientName);
        data.put("hospital_name", hospitalName);
        data.put("appointment_date", appointmentDate);
        data.put("appointment_time", appointmentTime);

        return sendNotification(fcmToken, title, body, data);
    }
}