package org.carefreepass.com.carefreepassserver.domain.notification.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.DeviceToken;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.NotificationHistory;
import org.carefreepass.com.carefreepassserver.domain.notification.repository.DeviceTokenRepository;
import org.carefreepass.com.carefreepassserver.domain.notification.repository.NotificationHistoryRepository;
import org.carefreepass.com.carefreepassserver.golbal.domain.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final MemberRepository memberRepository;
    private final FcmService fcmService;

    @Transactional
    public void registerDeviceToken(Long memberId, String fcmToken, String deviceType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        deviceTokenRepository.deactivateAllTokensByMemberId(memberId, Status.INACTIVE);

        Optional<DeviceToken> existingToken = deviceTokenRepository.findByFcmTokenAndStatus(fcmToken, Status.ACTIVE);
        if (existingToken.isPresent()) {
            existingToken.get().updateLastUsedAt();
        } else {
            DeviceToken deviceToken = DeviceToken.createDeviceToken(member, fcmToken, deviceType);
            deviceTokenRepository.save(deviceToken);
        }

        log.info("FCM token registered for member: {} (ID: {})", member.getName(), memberId);
    }

    public void sendAppointmentConfirmation(Long memberId, String memberName, String hospitalName, String appointmentDate, String appointmentTime) {
        Optional<DeviceToken> deviceToken = deviceTokenRepository.findActiveTokenByMemberId(memberId, Status.ACTIVE);
        if (deviceToken.isPresent()) {
            fcmService.sendAppointmentConfirmation(
                    deviceToken.get().getFcmToken(),
                    memberName,
                    hospitalName,
                    appointmentDate,
                    appointmentTime
            );
            log.info("Appointment confirmation sent to member: {} (ID: {})", memberName, memberId);
        }
    }

    @Transactional
    public boolean sendPatientCall(Long memberId, String memberName, String roomNumber, Long appointmentId) {
        Optional<DeviceToken> deviceToken = deviceTokenRepository.findActiveTokenByMemberId(memberId, Status.ACTIVE);

        if (deviceToken.isEmpty()) {
            throw new IllegalStateException("환자의 FCM 토큰이 없습니다.");
        }

        String title = "진료 호출";
        String message = String.format("%s님, %s로 들어오세요.", memberName, roomNumber);

        boolean success = fcmService.sendCallNotification(
                deviceToken.get().getFcmToken(),
                memberName,
                roomNumber
        );

        // 기존 appointment는 appointment 도메인에서 처리하므로 null로 전달
        NotificationHistory history;
        if (success) {
            history = NotificationHistory.createSuccess(null, title, message);
            log.info("Patient call sent successfully: {} (Appointment ID: {})", memberName, appointmentId);
        } else {
            history = NotificationHistory.createFailure(null, title, message, "FCM 전송 실패");
            log.error("Failed to send patient call: {} (Appointment ID: {})", memberName, appointmentId);
        }

        notificationHistoryRepository.save(history);
        return success;
    }

    public java.util.List<NotificationHistory> getNotificationHistory(Long appointmentId) {
        return notificationHistoryRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }
}