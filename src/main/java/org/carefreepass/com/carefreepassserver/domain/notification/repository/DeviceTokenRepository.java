package org.carefreepass.com.carefreepassserver.domain.notification.repository;

import java.util.List;
import java.util.Optional;
import org.carefreepass.com.carefreepassserver.domain.notification.entity.DeviceToken;
import org.carefreepass.com.carefreepassserver.golbal.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 디바이스 토큰 리포지토리
 * FCM 디바이스 토큰 정보에 대한 데이터베이스 접근을 담당합니다.
 * 토큰 등록, 조회, 비활성화, 상태 관리 등의 기능을 제공합니다.
 */
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    /**
     * 회원 ID와 상태로 디바이스 토큰 조회
     * 회원의 활성 상태 디바이스 토큰을 조회합니다.
     * 
     * @param memberId 회원 ID
     * @param status 토큰 상태
     * @return 디바이스 토큰
     */
    Optional<DeviceToken> findByMemberIdAndStatus(Long memberId, Status status);

    /**
     * 회원의 모든 디바이스 토큰 조회 (최근 사용순)
     * 회원의 모든 토큰을 최근 사용일 기준으로 정렬하여 조회합니다.
     * 
     * @param memberId 회원 ID
     * @param status 토큰 상태
     * @return 최근 사용순으로 정렬된 디바이스 토큰 목록
     */
    List<DeviceToken> findByMemberIdAndStatusOrderByLastUsedAtDesc(Long memberId, Status status);

    /**
     * FCM 토큰과 상태로 디바이스 토큰 조회
     * 특정 FCM 토큰이 이미 등록되어 있는지 확인할 때 사용합니다.
     * 
     * @param fcmToken FCM 디바이스 토큰
     * @param status 토큰 상태
     * @return 디바이스 토큰
     */
    Optional<DeviceToken> findByFcmTokenAndStatus(String fcmToken, Status status);

    /**
     * 회원의 모든 디바이스 토큰 비활성화
     * 새로운 디바이스 등록 시 기존 토큰들을 비활성화할 때 사용합니다.
     * 
     * @param memberId 회원 ID
     * @param status 변경할 상태
     * @return 업데이트된 레코드 수
     */
    @Modifying
    @Query("UPDATE DeviceToken d SET d.status = :status WHERE d.member.id = :memberId")
    int deactivateAllTokensByMemberId(@Param("memberId") Long memberId, @Param("status") Status status);

    /**
     * 특정 FCM 토큰의 상태 업데이트
     * 토큰 비활성화 또는 활성화 시 사용합니다.
     * 
     * @param fcmToken FCM 디바이스 토큰
     * @param newStatus 변경할 상태
     * @return 업데이트된 레코드 수
     */
    @Modifying
    @Query("UPDATE DeviceToken d SET d.status = :newStatus WHERE d.fcmToken = :fcmToken")
    int updateTokenStatus(@Param("fcmToken") String fcmToken, @Param("newStatus") Status newStatus);

    /**
     * FCM 토큰 존재 여부 확인
     * 중복 등록을 방지하기 위해 토큰의 존재 여부를 확인합니다.
     * 
     * @param fcmToken FCM 디바이스 토큰
     * @param status 토큰 상태
     * @return 존재 여부
     */
    boolean existsByFcmTokenAndStatus(String fcmToken, Status status);

    /**
     * 회원의 활성 디바이스 토큰 조회
     * 알림 전송 시 회원의 활성 토큰을 조회합니다.
     * 
     * @param memberId 회원 ID
     * @param status 토큰 상태
     * @return 활성 디바이스 토큰
     */
    @Query("SELECT d FROM DeviceToken d WHERE d.member.id = :memberId AND d.status = :status")
    Optional<DeviceToken> findActiveTokenByMemberId(@Param("memberId") Long memberId, @Param("status") Status status);
}