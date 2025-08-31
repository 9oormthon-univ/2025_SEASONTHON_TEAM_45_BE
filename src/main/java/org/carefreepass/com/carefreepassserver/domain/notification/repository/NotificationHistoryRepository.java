package org.carefreepass.com.carefreepassserver.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.NotificationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 알림 이력 리포지토리
 * FCM 알림 전송 이력에 대한 데이터베이스 접근을 담당합니다.
 * 알림 전송 성공/실패 기록, 통계 조회, 회원별/예약별 이력 관리 등의 기능을 제공합니다.
 */
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    /**
     * 특정 예약의 알림 이력 조회 (최신순)
     * 예약에 대한 모든 알림 전송 이력을 시간 순으로 조회합니다.
     * 예약 확인, 환자 호출 등의 알림 이력을 확인할 때 사용됩니다.
     * 
     * @param appointmentId 예약 ID
     * @return 생성시간 역순으로 정렬된 알림 이력 목록
     */
    List<NotificationHistory> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);

    /**
     * 성공/실패 상태별 알림 이력 페이징 조회 (최신순)
     * 알림 전송 성공 또는 실패한 이력을 페이징으로 조회합니다.
     * 관리자 페이지에서 알림 전송 현황을 모니터링할 때 사용됩니다.
     * 
     * @param isSuccess 성공 여부 (true: 성공, false: 실패)
     * @param pageable 페이징 정보
     * @return 페이징된 알림 이력 목록
     */
    Page<NotificationHistory> findByIsSuccessOrderByCreatedAtDesc(Boolean isSuccess, Pageable pageable);

    /**
     * 특정 기간 내 알림 이력 조회
     * 지정된 시작일과 종료일 사이의 모든 알림 이력을 조회합니다.
     * 일일/주간/월간 알림 통계를 생성할 때 사용됩니다.
     * 
     * @param startDate 조회 시작일시
     * @param endDate 조회 종료일시
     * @return 생성시간 역순으로 정렬된 기간 내 알림 이력
     */
    @Query("SELECT n FROM NotificationHistory n WHERE n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    List<NotificationHistory> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 날짜 이후 성공/실패 알림 개수 조회
     * 특정 날짜부터 현재까지의 알림 전송 성공 또는 실패 건수를 카운트합니다.
     * 대시보드의 통계 정보나 알림 전송 성공률 계산에 사용됩니다.
     * 
     * @param isSuccess 성공 여부
     * @param date 기준 날짜
     * @return 해당 조건의 알림 개수
     */
    @Query("SELECT COUNT(n) FROM NotificationHistory n WHERE n.isSuccess = :isSuccess AND n.createdAt >= :date")
    Long countByIsSuccessAndCreatedAtAfter(@Param("isSuccess") Boolean isSuccess, @Param("date") LocalDateTime date);

    /**
     * 특정 회원의 모든 알림 이력 조회 (최신순)
     * 회원이 받은 모든 알림 이력을 조회합니다.
     * 개별 환자의 알림 수신 이력을 확인할 때 사용됩니다.
     * 
     * @param memberId 회원 ID
     * @return 생성시간 역순으로 정렬된 해당 회원의 알림 이력
     */
    @Query("SELECT n FROM NotificationHistory n WHERE n.appointment.member.id = :memberId ORDER BY n.createdAt DESC")
    List<NotificationHistory> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    /**
     * 전체 알림 이력을 최신순으로 조회 (최신 100개로 제한)
     * 시스템 전체의 최근 알림 전송 이력을 제한된 개수로 조회합니다.
     * 관리자 대시보드의 최근 알림 현황 표시에 사용됩니다.
     * 
     * @return 최신 100개의 알림 이력 (생성시간 역순)
     */
    List<NotificationHistory> findTop100ByOrderByCreatedAtDesc();
}