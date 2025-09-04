package org.carefreepass.com.carefreepassserver.domain.appointment.scheduler;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.Appointment;
import org.carefreepass.com.carefreepassserver.domain.appointment.entity.AppointmentStatus;
import org.carefreepass.com.carefreepassserver.domain.appointment.service.AppointmentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 예약 상태 자동 관리 스케줄러
 * 매일 정해진 시간에 예약 상태를 자동으로 업데이트합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentScheduler {

    private final AppointmentService appointmentService;

    /**
     * 매일 오전 6시에 실행되는 예약 상태 업데이트 스케줄러
     * 오늘 날짜의 WAITING 예약을 SCHEDULED로 변경합니다.
     */
    @Scheduled(cron = "0 0 6 * * *") // 매일 오전 6시
    public void updateTodayAppointmentsToScheduled() {
        try {
            LocalDate today = LocalDate.now();
            log.info("예약 상태 자동 업데이트 시작 - 날짜: {}", today);
            
            // 오늘 날짜의 모든 예약 조회
            List<Appointment> todayAppointments = appointmentService.getAppointmentsByDate(today);
            
            int updatedCount = 0;
            for (Appointment appointment : todayAppointments) {
                // WAITING 상태인 예약만 SCHEDULED로 변경
                if (appointment.getStatus() == AppointmentStatus.WAITING) {
                    appointmentService.updateAppointmentStatus(appointment.getId(), AppointmentStatus.SCHEDULED);
                    updatedCount++;
                    log.debug("예약 상태 변경: {} (ID: {}) - WAITING → SCHEDULED", 
                             appointment.getMember().getName(), appointment.getId());
                }
            }
            
            log.info("예약 상태 자동 업데이트 완료 - 총 {}건 업데이트됨", updatedCount);
            
        } catch (Exception e) {
            log.error("예약 상태 자동 업데이트 중 오류 발생", e);
        }
    }

    /**
     * 매시간 정각에 실행되는 예약 상태 체크 (선택사항)
     * 필요시 추가적인 상태 관리 로직을 구현할 수 있습니다.
     */
    @Scheduled(cron = "0 0 * * * *") // 매시간 정각
    public void hourlyAppointmentCheck() {
        try {
            // 필요시 추가 로직 구현
            // 예: 예약 시간이 지난 SCHEDULED 예약을 다른 상태로 변경 등
            log.debug("시간별 예약 상태 체크 실행");
            
        } catch (Exception e) {
            log.error("시간별 예약 상태 체크 중 오류 발생", e);
        }
    }
}