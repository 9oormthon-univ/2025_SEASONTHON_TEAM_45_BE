package org.carefreepass.com.carefreepassserver.domain.notification.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.repository.AppointmentRepository;
import org.carefreepass.com.carefreepassserver.domain.member.entity.Member;
import org.carefreepass.com.carefreepassserver.domain.member.repository.MemberRepository;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.DeviceToken;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.NotificationHistory;
import org.carefreepass.com.carefreepassserver.domain.notification.repository.DeviceTokenRepository;
import org.carefreepass.com.carefreepassserver.domain.notification.repository.NotificationHistoryRepository;
import org.carefreepass.com.carefreepassserver.golbal.domain.Status;
import org.carefreepass.com.carefreepassserver.golbal.error.BusinessException;
import org.carefreepass.com.carefreepassserver.golbal.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스
 * FCM 토큰 관리, 환자 호출 알림, 예약 확인 알림 등을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final MemberRepository memberRepository;
    private final AppointmentRepository appointmentRepository;
    private final FcmService fcmService;

    /**
     * FCM 디바이스 토큰을 등록합니다.
     * 기존 토큰들을 비활성화하고 새로운 토큰을 활성화합니다. (한 환자당 하나의 활성 토큰 유지)
     * 
     * @param memberId 환자 ID
     * @param fcmToken FCM 토큰 문자열
     * @param deviceType 디바이스 타입 (ANDROID, IOS 등)
     * @throws BusinessException 존재하지 않는 회원인 경우 (MEMBER_NOT_FOUND)
     */
    @Transactional
    public void registerDeviceToken(Long memberId, String fcmToken, String deviceType) {
        // 회원 존재 여부 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 기존 토큰들을 모두 비활성화 (한 환자당 하나의 활성 토큰만 유지)
        deviceTokenRepository.deactivateAllTokensByMemberId(memberId, Status.INACTIVE);

        // 같은 토큰이 이미 활성화되어 있는지 확인
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByFcmTokenAndStatus(fcmToken, Status.ACTIVE);
        if (existingToken.isPresent()) {
            // 기존 토큰의 마지막 사용 시간 업데이트
            existingToken.get().updateLastUsedAt();
        } else {
            // 새로운 토큰 생성 및 저장
            DeviceToken deviceToken = DeviceToken.createDeviceToken(member, fcmToken, deviceType);
            deviceTokenRepository.save(deviceToken);
        }

        log.info("FCM 토큰 등록 완료: 회원 {} (ID: {})", member.getName(), memberId);
    }

    public void sendAppointmentConfirmation(Long appointmentId, Long memberId, String memberName, String hospitalName, String appointmentDate, String appointmentTime) {
        log.info("예약 확인 알림 전송 시도: 회원 {} (ID: {}) 예약 {} {} {}", memberName, memberId, hospitalName, appointmentDate, appointmentTime);
        
        Optional<DeviceToken> deviceToken = deviceTokenRepository.findActiveTokenByMemberId(memberId, Status.ACTIVE);
        if (deviceToken.isPresent()) {
            boolean sent = fcmService.sendAppointmentConfirmation(
                    deviceToken.get().getFcmToken(),
                    memberName,
                    hospitalName,
                    appointmentDate,
                    appointmentTime
            );
            
            // 알림 이력 저장 (예약 ID와 함께)
            String title = "예약 확인";
            String message = String.format("%s님, %s %s %s 예약이 완료되었습니다.", memberName, hospitalName, appointmentDate, appointmentTime);
            
            // 예약 조회
            Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
            
            NotificationHistory history;
            if (sent) {
                history = NotificationHistory.createSuccess(appointment.orElse(null), title, message);
            } else {
                history = NotificationHistory.createFailure(appointment.orElse(null), title, message, "FCM 전송 실패");
            }
            notificationHistoryRepository.save(history);
            
            if (sent) {
                log.info("✅ 예약 확인 알림 전송 성공: 회원 {} (ID: {})", memberName, memberId);
            } else {
                log.error("❌ 예약 확인 알림 전송 실패: 회원 {} (ID: {})", memberName, memberId);
            }
        } else {
            log.warn("⚠️ 활성 FCM 토큰을 찾을 수 없음: 회원 {} (ID: {})", memberName, memberId);
        }
    }

    /**
     * 환자 호출 알림을 전송합니다. (핵심 기능)
     * 환자의 활성 FCM 토큰으로 진료실 호출 알림을 전송하고 이력을 저장합니다.
     * 
     * @param memberId 환자 ID
     * @param memberName 환자 이름
     * @param roomNumber 진료실 번호
     * @param appointmentId 예약 ID (로그용)
     * @return 알림 전송 성공 여부
     * @throws BusinessException 활성 FCM 토큰이 없는 경우 (DEVICE_TOKEN_NOT_FOUND)
     */
    @Transactional
    public boolean sendPatientCall(Long memberId, String memberName, String roomNumber, Long appointmentId) {
        // 환자의 활성 FCM 토큰 조회
        Optional<DeviceToken> deviceToken = deviceTokenRepository.findActiveTokenByMemberId(memberId, Status.ACTIVE);

        if (deviceToken.isEmpty()) {
            throw new BusinessException(ErrorCode.DEVICE_TOKEN_NOT_FOUND);
        }

        // 알림 메시지 구성
        String title = "진료 호출";
        String message = String.format("%s님, %s로 들어오세요.", memberName, roomNumber);

        // FCM 서비스를 통한 푸시 알림 전송
        boolean success = fcmService.sendCallNotification(
                deviceToken.get().getFcmToken(),
                memberName,
                roomNumber
        );

        // 알림 전송 이력 저장 (성공/실패 모두 기록)
        // 실제 예약 정보 조회
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        
        NotificationHistory history;
        if (success) {
            history = NotificationHistory.createSuccess(appointment.orElse(null), title, message);
            log.info("환자 호출 알림 전송 성공: {} (예약 ID: {})", memberName, appointmentId);
        } else {
            history = NotificationHistory.createFailure(appointment.orElse(null), title, message, "FCM 전송 실패");
            log.error("환자 호출 알림 전송 실패: {} (예약 ID: {})", memberName, appointmentId);
        }

        notificationHistoryRepository.save(history);
        return success;
    }

    /**
     * 특정 예약의 알림 이력을 조회합니다.
     * @param appointmentId 예약 ID
     * @return 해당 예약의 알림 이력 목록 (최신순)
     */
    public java.util.List<NotificationHistory> getNotificationHistory(Long appointmentId) {
        return notificationHistoryRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }

    /**
     * 전체 알림 이력을 조회합니다.
     * 예약 ID가 제공되면 해당 예약의 이력만, 없으면 전체 이력을 반환합니다.
     * 
     * @param appointmentId 예약 ID (Optional)
     * @return 알림 이력 목록
     */
    public java.util.List<NotificationHistory> getAllNotificationHistory(Long appointmentId) {
        if (appointmentId != null) {
            return notificationHistoryRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
        } else {
            // 전체 이력 조회 (최신 100개로 제한)
            return notificationHistoryRepository.findTop100ByOrderByCreatedAtDesc();
        }
    }
}