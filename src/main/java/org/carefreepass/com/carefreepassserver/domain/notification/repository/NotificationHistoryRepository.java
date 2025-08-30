package org.carefreepass.com.carefreepassserver.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.NotificationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    List<NotificationHistory> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);

    Page<NotificationHistory> findByIsSuccessOrderByCreatedAtDesc(Boolean isSuccess, Pageable pageable);

    @Query("SELECT n FROM NotificationHistory n WHERE n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    List<NotificationHistory> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(n) FROM NotificationHistory n WHERE n.isSuccess = :isSuccess AND n.createdAt >= :date")
    Long countByIsSuccessAndCreatedAtAfter(@Param("isSuccess") Boolean isSuccess, @Param("date") LocalDateTime date);

    @Query("SELECT n FROM NotificationHistory n WHERE n.appointment.member.id = :memberId ORDER BY n.createdAt DESC")
    List<NotificationHistory> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    /**
     * 전체 알림 이력을 최신순으로 조회 (최신 100개로 제한)
     */
    List<NotificationHistory> findTop100ByOrderByCreatedAtDesc();
}